package cloud.apposs.gateway.util;

import cloud.apposs.gateway.GatewayHttpRequest;

import java.net.InetSocketAddress;

public final class WebUtil {
    public static final String KEY_REAL_IP = "X-Real-IP";

	/**
     * 获取请求路径
     */
    public static String getRequestPath(GatewayHttpRequest request) {
        String servletPath = request.getUri().getPath();
        return servletPath;
    }

    /**
     * 获取请求Host
     */
    public static String getRequestHost(GatewayHttpRequest request) {
        String host = request.getRemoteHost();
        // 如果host有端口号，去掉端口号，只匹配域名
        host = host.split(":")[0];
        return host;
    }

    /**
     * 获取真实客户端请求IP
     */
    public static String getRequestIp(GatewayHttpRequest request) {
        String ipAddress = request.getHeader(KEY_REAL_IP);
        if (ipAddress == null) {
            ipAddress = ((InetSocketAddress) request.getRemoteAddr()).getAddress().toString();
            ipAddress = ipAddress.substring(1, ipAddress.length());
        }
        return ipAddress;
    }
}
