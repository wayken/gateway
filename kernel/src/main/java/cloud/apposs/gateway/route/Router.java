package cloud.apposs.gateway.route;

import cloud.apposs.gateway.GatewayConstants;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.rules.DefaultRulesEngine;
import cloud.apposs.gateway.rules.Facts;
import cloud.apposs.gateway.rules.Rule;
import cloud.apposs.gateway.rules.RulesEngine;
import cloud.apposs.gateway.upstream.Upstream;
import cloud.apposs.gateway.util.WebUtil;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.logger.Logger;
import cloud.apposs.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理器路由映射，即路由表，负责实现Request请求和方法匹配
 */
public class    Router {
    /**
     * HTTP 请求与 Route 处理器的映射，
     * 数据结构为：
     * path -> List<Route>，利用此数据结构可以实现一个请求路径的多种匹配，实现根据不同的 Methods 和 Host 作不同的 Route 匹配
     */
    private final Map<String, List<Route>> routes = new ConcurrentHashMap<String, List<Route>>();

    /**
     * 参数Url匹配器
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final RulesEngine ruleEngine = new DefaultRulesEngine();

    /**
     * 获取请求对应的 Route 处理器，无法匹配则返回null
     */
    public Route matchRoute(GatewayHttpRequest request, GatewayHttpResponse response) throws Exception {
        String requestMethod = request.getMethod();
        String requestPath = WebUtil.getRequestPath(request);
        String requestHost = WebUtil.getRequestHost(request);

        // 根据请求域名、方法、路径先进行路径的精确匹配获取对应的 Route 路由
        List<Route> routeList = routes.get(requestPath);
        if (routeList != null) {
            // 请求路径有精确匹配并且只有一个，同时路由没有配置高级匹配规则，则直接返回
            if (routeList.size() == 1) {
                Route route = routeList.get(0);
                if (route.isEnable() && route.getRule() == null) {
                    return route;
                }
            } else {
                // 请求路径有多个匹配，则代表可能是 METHOD 或者 HOST 不同，获取 METHOD + HOST 匹配最精确的那个
                Route matchedRoute = handleGetMatchedRoute(request, routeList, requestMethod, requestHost);
                if (matchedRoute != null) {
                    return matchedRoute;
                }
            }
        }
        // 如果精确匹配的路由被关闭或者没有精确匹配路径，则需要遍历进行路径正则匹配
        List<Route> matchedRouteList = new ArrayList<>();
        for (String path : routes.keySet()) {
            routeList = routes.get(path);
            Iterator<Route> routeIterator = routeList.iterator();
            while (routeIterator.hasNext()) {
                Route route = routeIterator.next();
                boolean isPattern = route.isPattern();
                if (!isPattern) {
                    // 非正则路径在前面就已经匹配，不可能在这里命中
                    continue;
                }
                String routePath = route.getPath();
                // 路径必须正则参数匹配
                if (!pathMatcher.match(routePath, requestPath)) {
                    continue;
                }
                matchedRouteList.add(route);
            }
        }
        // 请求路径正则也没匹配到，直接返回空
        if (matchedRouteList.isEmpty()) {
            return null;
        }
        // 请求路径有正则匹配，获取METHOD+HOST匹配最精确的那个
        return handleGetMatchedRoute(request, matchedRouteList, requestMethod, requestHost);
    }

    /**
     * 添加路由，路由的存储结构是以请求路径为Key，对应的 {@link Route} 处理器列表为Value，
     * 一个请求路径可以有对应多个 {@link Route} 处理器，包括不同的Method和Host，以此来实现多种匹配，
     * 从路由表添加路由操作主要是在网关启动或者动态添加路由时调用
     *
     * @param  zone  区域
     * @param  route 路由
     * @return 添加成功返回true，否则返回false
     */
    public boolean addRoute(Zone zone, Route route) {
        if (route == null) {
            return false;
        }
        String path = handleRoutePathPattern(route);
        List<Route> routeList = routes.computeIfAbsent(path, k -> new ArrayList<Route>());
        routeList.add(route);
        Logger.info("Zone '%s' Add %s Success", zone.getId(), route);
        return true;
    }

    /**
     * 更新路由，主要用于动态更新路由信息
     *
     * @param  route 新路由
     * @return 更新成功返回true，否则返回false
     */
    public boolean updateRoute(Zone zone, Route route) {
        if (route == null) {
            return false;
        }
        String path = handleRoutePathPattern(route);
        List<Route> routeList = routes.get(path);
        if (routeList != null) {
            // 如果匹配到路由路径，则只需要更新路由的状态，同时关闭旧的路由
            for (int i = 0; i < routeList.size(); i++) {
                Route oldRoute = routeList.get(i);
                if (oldRoute.getId().equals(route.getId())) {
                    routeList.set(i, route);
                    oldRoute.shutdown();
                    Logger.info("Zone '%s' Update %s Success", zone.getId(), route);
                    return true;
                }
            }
            return false;
        }
        // 如果没有匹配到路由路径，则代表是修改了路由路径，需要先遍历路由表中匹配的旧路径路由ID，删除旧路径中的路由
        for (Map.Entry<String, List<Route>> oldRouteMapping : routes.entrySet()) {
            String oldRoutePath = oldRouteMapping.getKey();
            List<Route> oldRouteList = oldRouteMapping.getValue();
            for (Route oldRoute : oldRouteList) {
                if (!oldRoute.getId().equals(route.getId())) {
                    continue;
                }
                oldRouteList.remove(oldRoute);
                oldRoute.shutdown();
                break;
            }
            // 如果旧路径路由列表为空了，则从路由表中删除该路径下所有路由
            if (oldRouteList.isEmpty()) {
                routes.remove(oldRoutePath);
            }
        }
        // 添加新的路由到路由表
        List<Route> newRouteList = new ArrayList<>();
        newRouteList.add(route);
        routes.put(path, newRouteList);
        Logger.info("Zone '%s' Update %s Success", zone.getId(), route);
        return true;
    }

    /**
     * 删除路由，主要用于动态更新路由信息
     *
     * @param  routeId 路由ID
     * @return 删除成功返回true，否则返回false
     */
    public boolean removeRoute(Zone zone, String routeId) {
        if (routeId == null || routeId.trim().isEmpty()) {
            return false;
        }
        for (List<Route> routeList : routes.values()) {
            for (Route route : routeList) {
                if (route.getId().equals(routeId)) {
                    routeList.remove(route);
                    Logger.info("Zone '%s' Remove %s Success", zone.getId(), route);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 更新路由上游，主要用于动态更新路由信息
     *
     * @param  upstream 上游服务
     * @return 更新成功返回true，否则返回false
     */
    public boolean updateUpstream(Zone zone, Upstream upstream) {
        if (upstream == null) {
            return false;
        }
        for (List<Route> routeList : routes.values()) {
            for (Route route : routeList) {
                if (route.upstreamId().equals(upstream.getId())) {
                    route.upstream(upstream);
                    Logger.info("Zone '%s' Update %s from %s Success", zone.getId(), upstream, route);
                }
            }
        }
        return true;
    }

    /**
     * 删除上游，主要用于动态更新路由信息
     *
     * @param  upstreamId 上游服务ID
     * @return 删除成功返回true，否则返回false
     */
    public boolean removeUpstream(Zone zone, String upstreamId) {
        if (upstreamId == null || upstreamId.trim().isEmpty()) {
            return false;
        }
        for (List<Route> routeList : routes.values()) {
            for (Route route : routeList) {
                if (route.upstreamId().equals(upstreamId)) {
                    route.upstream(null);
                    Logger.info("Zone '%s' Remove %s from %s Success", zone.getId(), upstreamId, route);
                }
            }
        }
        return true;
    }

    /**
     * 判断路由路径是否为正则路径，是则标记路由为正则路径
     *
     * @param  route 路由
     * @return 返回路由路径
     */
    public String handleRoutePathPattern(Route route) {
        String path = route.getPath();
        boolean isPattern = pathMatcher.isPattern(path);
        route.setPattern(isPattern);
        return path;
    }

    /**
     * 对路径匹配的 {@link Route} 进行规则匹配，排序规则如下：
     * <pre>
     * 1. STATUS为0的路由（即路由未启用）不做匹配
     * 2. METHOD不为空且无法精确匹配到对应的METHOD时，不做匹配
     * 3. METHOD精确匹配+3分，METHOD正则匹配+2分
     * 4. HOST精确匹配+3分，HOST AntPath匹配+2分，HOST *泛匹配+1分
     * 5. 如果有配置路由高级匹配规则，则进行高级匹配，匹配则+1分
     * </pre>
     * 返回分数最高的 {@link Route}，如果分数相同则返回第一个匹配的 {@link Route}
     *
     * @param  request 请求
     * @param  routes  初次匹配请求路径的路由列表
     * @param  method  请求方法
     * @param  host    请求域名
     * @return 返回匹配的 {@link Route}，无法匹配则返回null
     */
    private Route handleGetMatchedRoute(GatewayHttpRequest request, List<Route> routes, String method, String host) throws Exception {
        int matchedScore = 0;
        Route matchedRoute = null;
        for (Route route : routes) {
            // 路由状态为0则不做匹配，直接跳过
            if (!route.isEnable()) {
                continue;
            }
            int score = 0;
            // 如果有配置路由高级匹配规则，进行高级匹配，不匹配则跳过
            Rule rule = route.getRule();
            if (rule != null) {
                Facts facts = (Facts) request.getAttribute(GatewayConstants.REQUEST_ATTRIBUTE_RULES_FACTS);
                boolean matched = ruleEngine.fire(rule, facts);
                if (!matched) {
                    continue;
                } else {
                    score++;
                }
            }
            // 方法匹配
            Route.Method[] metthods = route.getMethods();
            if (metthods != null) {
                boolean matched = false;
                for (Route.Method routeMethod : metthods) {
                    if (routeMethod.matched(method)) {
                        matched = true;
                        break;
                    }
                }
                if (matched) {
                    score += 3;
                } else if (GatewayConstants.PATH_MATCHER_ANY.equals(method)) {
                    score += 2;
                } else {
                    continue;
                }
            }
            // 匹配分数最高的那个，分数相同则获取优先级最小的那个
            if (score > matchedScore) {
                matchedScore = score;
                matchedRoute = route;
            } else if (score == matchedScore) {
                if (matchedRoute == null || route.getPriority() < matchedRoute.getPriority()) {
                    matchedRoute = route;
                }
            }
        }
        return matchedRoute;
    }
}
