package cloud.apposs.gateway.route;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayException;
import cloud.apposs.gateway.upstream.UpstreamConstant;
import cloud.apposs.gateway.upstream.UpstreamParser;
import cloud.apposs.util.HttpStatus;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;

public final class RouteParser {
    /**
     * 解析路由配置，转换为路由对象，内容格式如下：
     * <pre>
     * {
     *   "host": "www.apposs.cloud",
     *   "path": "/api/v1/user",
     *   "methods": ["GET,POST"],
     *   "status": 1,
     *   "upstream_id": "1"
     * }
     * </pre>
     *
     * @param  id      路由ID
     * @param  content 路由配置内容
     * @param  context 网关上下文
     * @return 路由对象
     * @throws Exception 如果配置不符合规范则抛出异常
     */
    public static Route parse(String id, String content, GatewayContext context) throws Exception {
        Param routeInfo = JsonUtil.parseJsonParam(content);
        if (routeInfo == null || routeInfo.isEmpty()) {
            throw new GatewayException(HttpStatus.HTTP_STATUS_500, "Route: " + id + " config is invalid");
        }
        // 解析路由基本配置
        String path = routeInfo.getString(RouteConstant.KEY_PATH);
        Table<String> methods = routeInfo.getTable(RouteConstant.KEY_METHODS);
        String rule = routeInfo.getString(RouteConstant.KEY_RULE);
        int priority = routeInfo.getInt(RouteConstant.KEY_PRIORITY, 0);
        byte status = routeInfo.getByte(RouteConstant.KEY_STATUS, RouteConstant.STATUS_ENABLE);
        Route.Method[] methodList = null;
        if (methods != null) {
            methodList = new Route.Method[methods.size()];
            for (int i = 0; i < methods.size(); i++) {
                methodList[i] = Route.Method.valueOf(methods.get(i).trim().toUpperCase());
            }
        }
        Route route = new Route(id, path, methodList, rule, priority, status);
        // 解析认证配置
        String authId = routeInfo.getString(RouteConstant.KEY_AUTH_ID);
        route.authId(authId);
        // 解析路由上游配置
        String upstreamId = routeInfo.getString(RouteConstant.KEY_UPSTREAM_ID);
        route.upstreamId(upstreamId);
        if (isManualUpstream(route)) {
            // 手动配置上游
            Param upstream = routeInfo.getParam(RouteConstant.KEY_UPSTREAM).setString(UpstreamConstant.KEY_NAME, "ManualUpstream");
            if (upstream != null) {
                route.upstream(UpstreamParser.parse(upstreamId, upstream, context));
            }
        }
        return route;
    }

    /**
     * 判断是否为手动配置上游
     */
    public static boolean isManualUpstream(Route route) {
        return RouteConstant.UPSTREAM_ID_MANUAL.equals(route.upstreamId());
    }
}
