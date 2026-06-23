package cloud.apposs.gateway.util;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Objects;

/**
 * IP地址过滤器，支持IPv4和IPv6的CIDR匹配
 */
public final class IpSubnetFilter {
    private final IpFilterRule filterRule;
    private final String ipAddress;

    public IpSubnetFilter(String cidr) {
        String ipAddress = cidr;
        int cidrPrefix = -1;
        int slashPos = cidr.indexOf('/');
        if (slashPos > 0) {
            ipAddress = cidr.substring(0, slashPos);
            cidrPrefix = Integer.parseInt(cidr.substring(slashPos + 1));
        }
        try {
            this.ipAddress = ipAddress;
            this.filterRule = selectFilterRule(getAddressByName(ipAddress), cidrPrefix);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("ipAddress", e);
        }
    }

    public IpSubnetFilter(String ipAddress, int cidrPrefix) {
        try {
            this.ipAddress = ipAddress;
            this.filterRule = selectFilterRule(getAddressByName(ipAddress), cidrPrefix);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("ipAddress", e);
        }
    }

    public boolean matches(String remoteAddress) {
        try {
            return matches(getAddressByName(remoteAddress));
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public boolean matches(InetAddress remoteAddress) {
        return filterRule.matches(remoteAddress);
    }

    @Override
    public String toString() {
        return "IpSubnetFilter{" +
                "filterRule=" + filterRule +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }

    private static IpFilterRule selectFilterRule(InetAddress ipAddress, int cidrPrefix) {
        Objects.requireNonNull(ipAddress, "ipAddress");
        if (cidrPrefix < 0) {
            return new IpDirectFilterRule(ipAddress);
        }
        if (ipAddress instanceof Inet4Address) {
            return new Ip4SubnetFilterRule((Inet4Address) ipAddress, cidrPrefix);
        } else if (ipAddress instanceof Inet6Address) {
            return new Ip6SubnetFilterRule((Inet6Address) ipAddress, cidrPrefix);
        } else {
            throw new IllegalArgumentException("Only IPv4 and IPv6 addresses are supported");
        }
    }

    public interface IpFilterRule {
        boolean matches(InetAddress remoteAddress);
    }

    // IPv4地址过滤规则
    static final class Ip4SubnetFilterRule implements IpFilterRule {
        private final int networkAddress;
        private final int subnetMask;

        private Ip4SubnetFilterRule(Inet4Address ipAddress, int cidrPrefix) {
            if (cidrPrefix < 0 || cidrPrefix > 32) {
                throw new IllegalArgumentException(String.format("IPv4 requires the subnet prefix to be in range of "
                        + "[0,32]. The prefix was: %d", cidrPrefix));
            }
            subnetMask = prefixToSubnetMask(cidrPrefix);
            networkAddress = ipv4AddressToInt(ipAddress) & subnetMask;
        }

        @Override
        public boolean matches(InetAddress remoteAddress) {
            final InetAddress inetAddress = remoteAddress;
            if (inetAddress instanceof Inet4Address) {
                int ipAddress = ipv4AddressToInt((Inet4Address) inetAddress);
                return (ipAddress & subnetMask) == networkAddress;
            }
            return false;
        }

        private static int prefixToSubnetMask(int cidrPrefix) {
            return (int) (-1L << 32 - cidrPrefix);
        }

        @Override
        public String toString() {
            return "Ip4SubnetFilterRule{" +
                    "networkAddress=" + networkAddress +
                    ", subnetMask=" + subnetMask +
                    '}';
        }
    }

    // IPv6地址过滤规则
    static final class Ip6SubnetFilterRule implements IpFilterRule {
        private static final BigInteger MINUS_ONE = BigInteger.valueOf(-1);

        private final BigInteger networkAddress;
        private final BigInteger subnetMask;

        private Ip6SubnetFilterRule(Inet6Address ipAddress, int cidrPrefix) {
            if (cidrPrefix < 0 || cidrPrefix > 128) {
                throw new IllegalArgumentException(String.format("IPv6 requires the subnet prefix to be in range of "
                        + "[0,128]. The prefix was: %d", cidrPrefix));
            }
            subnetMask = prefixToSubnetMask(cidrPrefix);
            networkAddress = ipToInt(ipAddress).and(subnetMask);
        }

        @Override
        public boolean matches(InetAddress remoteAddress) {
            if (remoteAddress instanceof Inet6Address) {
                BigInteger ipAddress = ipToInt((Inet6Address) remoteAddress);
                return ipAddress.and(subnetMask).equals(subnetMask) || ipAddress.and(subnetMask).equals(networkAddress);
            }
            return false;
        }

        private static BigInteger ipToInt(Inet6Address ipAddress) {
            byte[] octets = ipAddress.getAddress();
            assert octets.length == 16;

            return new BigInteger(octets);
        }

        private static BigInteger prefixToSubnetMask(int cidrPrefix) {
            return MINUS_ONE.shiftLeft(128 - cidrPrefix);
        }

        @Override
        public String toString() {
            return "Ip6SubnetFilterRule{" +
                    "networkAddress=" + networkAddress +
                    ", subnetMask=" + subnetMask +
                    '}';
        }
    }

    // 直接IP地址过滤规则
    static final class IpDirectFilterRule implements IpFilterRule {
        private final InetAddress ipAddress;

        IpDirectFilterRule(InetAddress ipAddress) {
            this.ipAddress = ipAddress;
        }

        @Override
        public boolean matches(InetAddress remoteAddress) {
            return ipAddress.equals(remoteAddress);
        }
    }

    private static int ipv4AddressToInt(Inet4Address ipAddress) {
        byte[] octets = ipAddress.getAddress();
        return  (octets[0] & 0xff) << 24 |
                (octets[1] & 0xff) << 16 |
                (octets[2] & 0xff) << 8 |
                octets[3] & 0xff;
    }

    private static InetAddress getAddressByName(final String hostname) throws UnknownHostException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<InetAddress>() {
                @Override
                public InetAddress run() throws UnknownHostException {
                    return InetAddress.getByName(hostname);
                }
            });
        } catch (PrivilegedActionException e) {
            throw (UnknownHostException) e.getCause();
        }
    }
}
