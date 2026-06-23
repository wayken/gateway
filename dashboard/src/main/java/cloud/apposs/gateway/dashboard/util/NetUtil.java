package cloud.apposs.gateway.dashboard.util;

import cloud.apposs.bootor.BootorHttpRequest;

import java.net.InetSocketAddress;

public final class NetUtil {
    public static String getRequestIp(BootorHttpRequest request) {
        String ip = null;
        if (request.isHeaderContains("X-Real-IP")) {
            ip = request.getHeader("X-Real-IP");
        } else {
            ip = ((InetSocketAddress) request.getRemoteAddr()).getAddress().toString();
        }
        if (ip.startsWith("/")) {
            ip = ip.substring(1);
        }
        return ip;
    }
}
