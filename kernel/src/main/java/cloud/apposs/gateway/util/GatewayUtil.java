package cloud.apposs.gateway.util;

import cloud.apposs.gateway.GatewayConstants;

public final class GatewayUtil {
	/**
     * 获取网关网域路径
     */
    public static String getZonesPath(String path) {
        return path + GatewayConstants.GATEWAY_NODE_ZONE;
    }

    /**
     * 获取网关全局配置路径
     */
    public static String getGlobalPath(String path) {
        return path + GatewayConstants.GATEWAY_NODE_GLOBAL;
    }

    /**
     * 获取网关服务注册路径
     */
    public static String getServicesPath(String path) {
        return path + GatewayConstants.GATEWAY_NODE_SERVICE;
    }

    /**
     * 通过网关全路径获取网关ID，
     * 如：/gateway/zones/{zoneId}/route/xxx、C:/gateway/zones/{zoneId}/route/xxx、C:\gateway\zones\{zoneId}\route\xxx等
     */
    public static String getZoneIdByPath(String path) {
        // 先将路径转换为标准路径
        path = path.replace('\\', '/');
        // 获取zoneId
        int index = path.indexOf(GatewayConstants.GATEWAY_NODE_ZONE);
        if (index == -1) {
            return null;
        }
        int start = index + GatewayConstants.GATEWAY_NODE_ZONE.length() + 1;
        int end = path.indexOf('/', start);
        if (end == -1) {
            end = path.length();
        }
        return path.substring(start, end);
    }
}
