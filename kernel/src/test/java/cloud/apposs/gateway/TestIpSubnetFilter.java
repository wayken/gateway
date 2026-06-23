package cloud.apposs.gateway;

import cloud.apposs.gateway.util.IpSubnetFilter;
import org.junit.Assert;
import org.junit.Test;

public class TestIpSubnetFilter {
    @Test
    public void testIpv4CidrMatch() {
        String ip = "192.168.1.1";
        String cidr = "192.168.1.0/24";
        IpSubnetFilter filter = new IpSubnetFilter(cidr);
        boolean result = filter.matches(ip);
        Assert.assertTrue(result);
    }

    @Test
    public void testIpv4CidrMatch2() {
        String ip = "192.168.2.1";
        String cidr = "192.168.1.0/24";
        IpSubnetFilter filter = new IpSubnetFilter(cidr);
        boolean result = filter.matches(ip);
        Assert.assertFalse(result);
    }

    @Test
    public void testIpvDirectMatch() {
        String ip = "192.168.2.1";
        String cidr = "192.168.2.1";
        IpSubnetFilter filter = new IpSubnetFilter(cidr);
        boolean result = filter.matches(ip);
        Assert.assertTrue(result);
    }
}
