package cloud.apposs.gateway.plugin.runner.rewrite;

import cloud.apposs.gateway.GatewayConstants;
import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.annotation.CommonPlugin;
import cloud.apposs.gateway.plugin.PluginAdapter;
import cloud.apposs.gateway.plugin.PluginResult;
import cloud.apposs.gateway.plugin.runner.rewrite.action.RequestRemoveHeaderAction;
import cloud.apposs.gateway.plugin.runner.rewrite.action.RequestSetHeaderAction;
import cloud.apposs.gateway.plugin.runner.rewrite.action.ResponseRemoveHeaderAction;
import cloud.apposs.gateway.plugin.runner.rewrite.action.ResponseSetHeaderAction;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.rules.*;
import cloud.apposs.gateway.rules.http.HttpMVELRule;
import cloud.apposs.gateway.rules.mvel.MVELRule;
import cloud.apposs.gateway.util.GatewayUtil;
import cloud.apposs.gateway.watch.IWatch;
import cloud.apposs.gateway.watch.WatchEventType;
import cloud.apposs.gateway.watch.WatchListener;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.logger.Logger;
import cloud.apposs.react.React;
import cloud.apposs.util.FileUtil;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 重写请求路径插件，将请求路径重写为指定的路径
 * 修改请求头规则格式如下：
 * /gateway/xxx.com/rewrite/request/{name}
 * <pre>
 * {
 *   "status: 1,
 *   "route": [1, 2]
 *   "rule": "if request.host == 'xxx.com' && request.path = '/xxx/'",
 *   "action": [
 *     {
 *       "type": "static",
 *       "header": {
 *         "name": "Content-Type",
 *         "value": "application/json"
 *       }
 *     },
 *     {
 *       "type": "remove",
 *       "header": {
 *         "name": "X-Forwarded-For"
 *       }
 *     },
 *     {
 *       "type": "dynamic",
 *       "header": {
 *         "name": "X-Real-IP",
 *         "value": "request.remote_addr"
 *       }
 *     }
 *   ]
 * }
 * </pre>
 */
@CommonPlugin
public class RewritePlugin extends PluginAdapter {
    public static final String NAME = "Rewrite";
    private static final int PLUGIN_PRIORITY = 1090;

    /**
     * 自定义规则集合，用于存储所有的规则，每个Zone对应一个规则集合
     */
    private final Map<String, Rules> rewriteRequestRuleMapping = new ConcurrentHashMap<>();
    private final Map<String, Rules> rewriteResponseRuleMapping = new ConcurrentHashMap<>();

    private final RulesEngine ruleEngine = new DefaultRulesEngine();

    public RewritePlugin() {
        super(NAME);
    }

    @Override
    public int getPriority() {
        return PLUGIN_PRIORITY;
    }

    @Override
    public void registerWatcher(Zone zone, GatewayContext context, IWatch watcher) throws Exception {
        // 监听修改请求头规则目录
        String requestRulePath = GatewayUtil.getZonesPath(context.getConfig().getWatchPath()) + "/" + zone.getId() + RewriteConstant.REWRITE_REQUEST_PATH;
        watcher.addListener(requestRulePath, new RewriteRequestRuleListener());
        // 监听修改响应头规则目录
        String responseRulePath = GatewayUtil.getZonesPath(context.getConfig().getWatchPath()) + "/" + zone.getId() + RewriteConstant.REWRITE_RESPONSE_PATH;
        watcher.addListener(responseRulePath, new RewriteResponseRuleListener());
    }

    @Override
    public React<PluginResult> preFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) {
        return React.emitter(() -> {
            Rules rules = rewriteRequestRuleMapping.get(zone.getId());
            if (rules == null || rules.isEmpty()) {
                return PluginResult.SUCCESS;
            }
            Facts facts = (Facts) request.getAttribute(GatewayConstants.REQUEST_ATTRIBUTE_RULES_FACTS);
            ruleEngine.fire(rules, facts, route, request, response);
            return PluginResult.SUCCESS;
        });
    }

    @Override
    public void postFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route, Object value) throws Exception {
        Rules rules = rewriteResponseRuleMapping.get(zone.getId());
        if (rules == null || rules.isEmpty()) {
            return;
        }
        Facts facts = (Facts) request.getAttribute(GatewayConstants.REQUEST_ATTRIBUTE_RULES_FACTS);
        ruleEngine.fire(rules, facts, route, request, response);
    }

    private class RewriteRequestRuleListener implements WatchListener {
        @Override
        public void onNodeChanged(String path, byte[] data, WatchEventType event) {
            if (event.matched(WatchEventType.ADDED)) {
                String ruleId = FileUtil.splitFileName(path);
                String content = new String(data);
                Param ruleInfo = JsonUtil.parseJsonParam(content);
                String ruleData = ruleInfo.getString(RewriteConstant.KEY_RULE);
                int ruleStatus = ruleInfo.getInt(RewriteConstant.KEY_STATUS);
                List<String> routeIdList = ruleInfo.getList(RewriteConstant.KEY_ROUTES);
                MVELRule rule = new HttpMVELRule(ruleId, ruleStatus, routeIdList).when(ruleData);
                List<Param> actions = ruleInfo.getList(RewriteConstant.KEY_ACTION);
                for (Param action : actions) {
                    rule.then(handleNewRequestRuleAction(action));
                }
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                Rules rules= rewriteRequestRuleMapping.computeIfAbsent(zoneId, k -> new Rules());
                rules.register(rule);
                Logger.info("Rewrite Request Rule Added: %s", rule);
            } else if (event.matched(WatchEventType.UPDATED)) {
                String content = new String(data);
                Param ruleInfo = JsonUtil.parseJsonParam(content);
                String ruleData = ruleInfo.getString(RewriteConstant.KEY_RULE);
                String ruleId = FileUtil.splitFileName(path);
                int ruleStatus = ruleInfo.getInt(RewriteConstant.KEY_STATUS);
                List<String> routeIdList = ruleInfo.getList(RewriteConstant.KEY_ROUTES);
                MVELRule rule = new HttpMVELRule(ruleId, ruleStatus, routeIdList).when(ruleData);
                List<Param> actions = ruleInfo.getList(RewriteConstant.KEY_ACTION);
                for (Param action : actions) {
                    rule.then(handleNewRequestRuleAction(action));
                }
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                Rules rules= rewriteRequestRuleMapping.computeIfAbsent(zoneId, k -> new Rules());
                rules.update(rule);
                Logger.info("Rewrite Request Rule Updated: %s", rule);
            } else if (event.matched(WatchEventType.DELETED)) {
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                Rules rules = rewriteRequestRuleMapping.computeIfAbsent(zoneId, k -> new Rules());
                String ruleId = FileUtil.splitFileName(path);
                rules.unregister(ruleId);
                Logger.info("Rewrite Request Rule Deleted: %s", ruleId);
            }
        }

        private Action handleNewRequestRuleAction(Param parameters) {
            String actionType = parameters.getString(RewriteConstant.KEY_ACTION_TYPE);
            if (RewriteConstant.KEY_ACTION_TYPE_REMOVE.equals(actionType)) {
                String headerName = parameters.getString(RewriteConstant.KEY_ACTION_HEADER_NAME);
                return new RequestRemoveHeaderAction(headerName);
            } else if (RewriteConstant.KEY_ACTION_TYPE_STATIC.equals(actionType)) {
                String headerName = parameters.getString(RewriteConstant.KEY_ACTION_HEADER_NAME);
                String headerValue = parameters.getString(RewriteConstant.KEY_ACTION_HEADER_VALUE);
                return new RequestSetHeaderAction(false, headerName, headerValue);
            } else if (RewriteConstant.KEY_ACTION_TYPE_DYNAMIC.equals(actionType)) {
                String headerName = parameters.getString(RewriteConstant.KEY_ACTION_HEADER_NAME);
                String headerValue = parameters.getString(RewriteConstant.KEY_ACTION_HEADER_VALUE);
                return new RequestSetHeaderAction(true, headerName, headerValue);
            }
            return null;
        }
    }

    private class RewriteResponseRuleListener implements WatchListener {
        @Override
        public void onNodeChanged(String path, byte[] data, WatchEventType event) {
            if (event.matched(WatchEventType.ADDED)) {
                String ruleId = FileUtil.splitFileName(path);
                String content = new String(data);
                Param ruleInfo = JsonUtil.parseJsonParam(content);
                String ruleData = ruleInfo.getString(RewriteConstant.KEY_RULE);
                int ruleStatus = ruleInfo.getInt(RewriteConstant.KEY_STATUS);
                List<String> routeIdList = ruleInfo.getList(RewriteConstant.KEY_ROUTES);
                MVELRule rule = new HttpMVELRule(ruleId, ruleStatus, routeIdList).when(ruleData);
                List<Param> actions = ruleInfo.getList(RewriteConstant.KEY_ACTION);
                for (Param action : actions) {
                    rule.then(handleNewResponseRuleAction(action));
                }
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                Rules rules= rewriteResponseRuleMapping.computeIfAbsent(zoneId, k -> new Rules());
                rules.register(rule);
                Logger.info("Rewrite Response Rule Added: %s", rule);
            } else if (event.matched(WatchEventType.UPDATED)) {
                String content = new String(data);
                Param ruleInfo = JsonUtil.parseJsonParam(content);
                String ruleData = ruleInfo.getString(RewriteConstant.KEY_RULE);
                String ruleId = FileUtil.splitFileName(path);
                int ruleStatus = ruleInfo.getInt(RewriteConstant.KEY_STATUS);
                List<String> routeIdList = ruleInfo.getList(RewriteConstant.KEY_ROUTES);
                MVELRule rule = new HttpMVELRule(ruleId, ruleStatus, routeIdList).when(ruleData);
                List<Param> actions = ruleInfo.getList(RewriteConstant.KEY_ACTION);
                for (Param action : actions) {
                    rule.then(handleNewResponseRuleAction(action));
                }
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                Rules rules= rewriteResponseRuleMapping.computeIfAbsent(zoneId, k -> new Rules());
                rules.update(rule);
                Logger.info("Rewrite Response Rule Updated: %s", rule);
            } else if (event.matched(WatchEventType.DELETED)) {
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                Rules rules = rewriteResponseRuleMapping.computeIfAbsent(zoneId, k -> new Rules());
                String ruleId = FileUtil.splitFileName(path);
                rules.unregister(ruleId);
                Logger.info("Rewrite Response Rule Deleted: %s", ruleId);
            }
        }

        private Action handleNewResponseRuleAction(Param parameters) {
            String actionType = parameters.getString(RewriteConstant.KEY_ACTION_TYPE);
            if (RewriteConstant.KEY_ACTION_TYPE_REMOVE.equals(actionType)) {
                String headerName = parameters.getString(RewriteConstant.KEY_ACTION_HEADER_NAME);
                return new ResponseRemoveHeaderAction(headerName);
            } else if (RewriteConstant.KEY_ACTION_TYPE_STATIC.equals(actionType)) {
                String headerName = parameters.getString(RewriteConstant.KEY_ACTION_HEADER_NAME);
                String headerValue = parameters.getString(RewriteConstant.KEY_ACTION_HEADER_VALUE);
                return new ResponseSetHeaderAction(false, headerName, headerValue);
            } else if (RewriteConstant.KEY_ACTION_TYPE_DYNAMIC.equals(actionType)) {
                String headerName = parameters.getString(RewriteConstant.KEY_ACTION_HEADER_NAME);
                String headerValue = parameters.getString(RewriteConstant.KEY_ACTION_HEADER_VALUE);
                return new ResponseSetHeaderAction(true, headerName, headerValue);
            }
            return null;
        }
    }
}
