package cloud.apposs.gateway.rules.http;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.global.ips.IpAddress;
import cloud.apposs.gateway.util.IpSubnetFilter;

import java.net.InetSocketAddress;
import java.util.List;

public class HttpSrcIpRuleHandler {
    private final GatewayContext context;

    private final GatewayHttpRequest request;

    public HttpSrcIpRuleHandler(GatewayContext context, GatewayHttpRequest request) {
        this.context = context;
        this.request = request;
    }

    public boolean eq(String ip) {
        return ip.equals(getIpAddr(request));
    }

    public boolean ne(String ip) {
        return !eq(ip);
    }

    public boolean in(String... ips) {
        String ip = getIpAddr(request);
        for (String ipItem : ips) {
            if (matchCidr(ip, ipItem)) {
                return true;
            }
        }
        return false;
    }

    public boolean notIn(String... ips) {
        return !in(ips);
    }

    public boolean inList(String ipName) {
        List<IpAddress> ipList = context.getGlobal().getIps();
        IpAddress matchedIp = null;
        for (IpAddress ipItem : ipList) {
            if (ipItem.getName().equals(ipName)) {
                matchedIp = ipItem;
                break;
            }
        }
        if (matchedIp == null) {
            return false;
        }
        String ip = getIpAddr(request);
        List<IpSubnetFilter> cidrs = matchedIp.getCidrList();
        for (IpSubnetFilter cidr : cidrs) {
            if (cidr.matches(ip)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchCidr(String ip, String cidr) {
        IpSubnetFilter filter = new IpSubnetFilter(cidr);
        return filter.matches(ip);
    }

    private static String getIpAddr(GatewayHttpRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = ((InetSocketAddress) request.getRemoteAddr()).getAddress().getHostAddress();
        }
        return ip;
    }
}
