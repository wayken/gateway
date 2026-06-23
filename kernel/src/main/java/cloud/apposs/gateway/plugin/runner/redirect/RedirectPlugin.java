package cloud.apposs.gateway.plugin.runner.redirect;

import cloud.apposs.gateway.GatewayConstants;
import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.annotation.CommonPlugin;
import cloud.apposs.gateway.plugin.PluginAdapter;
import cloud.apposs.gateway.plugin.PluginResult;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.rules.*;
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
 * 请求重定向插件，将请求进行URL重定向
 * 规则格式如下：
 * /gateway/xxx.com/redirect/{name}
 * <pre>
 * {
 *   "status: 1,
 *   "route": [1, 2],
 *   "mode": "pattern" | "mvel",
 *   "rule": "if request.host == 'xxx.com' && request.path = '/xxx/'" || "https://www.(.*)",
 *   "action": [
 *     {
 *       "redirectUrl": "https://example.com/${1}/${2}" || "/download",
 *       "redirectStatus": 301
 *     }
 *   ]
 * }
 * </pre>
 */
@CommonPlugin
public class RedirectPlugin extends PluginAdapter {
    public static final String NAME = "Redirect";
    private static final int PLUGIN_PRIORITY = 1060;

    /**
     * 自定义规则集合，用于存储所有的规则，每个Zone对应一个规则集合
     */
    private final Map<String, Rules> ruleMapping = new ConcurrentHashMap<>();

    private final RulesEngine ruleEngine = new DefaultRulesEngine();

    public RedirectPlugin() {
        super(NAME);
    }

    @Override
    public int getPriority() {
        return PLUGIN_PRIORITY;
    }

    @Override
    public void registerWatcher(Zone zone, GatewayContext context, IWatch watcher) throws Exception {
        // 监听规则目录
        String cacheRulePath = GatewayUtil.getZonesPath(context.getConfig().getWatchPath()) + "/" + zone.getId() + RedirectConstant.DEFAULT_REDIRECT_PATH;
        watcher.addListener(cacheRulePath, new RedirectRuleListener());
    }

    @Override
    public React<PluginResult> preFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) {
        return React.emitter(() -> {
            Rules rules = ruleMapping.get(zone.getId());
            if (rules == null || rules.isEmpty()) {
                return PluginResult.SUCCESS;
            }
            Facts facts = (Facts) request.getAttribute(GatewayConstants.REQUEST_ATTRIBUTE_RULES_FACTS);
            boolean matched = ruleEngine.fire(rules, facts, route, request, response);
            if (matched) {
                response.write("", true);
                return PluginResult.SKIP;
            }
            return PluginResult.SUCCESS;
        });
    }

    private class RedirectRuleListener implements WatchListener {
        @Override
        public void onNodeChanged(String path, byte[] data, WatchEventType event) {
            if (event.matched(WatchEventType.ADDED)) {
                String ruleId = FileUtil.splitFileName(path);
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                String content = new String(data);
                Param ruleInfo = JsonUtil.parseJsonParam(content);
                // 解析动作
                String ruleMode = ruleInfo.getString(RedirectConstant.KEY_MODE);
                Param ruleAction = ruleInfo.getParam(RedirectConstant.KEY_ACTION);
                // 创建规则
                String ruleData = ruleInfo.getString(RedirectConstant.KEY_RULE);
                int ruleStatus = ruleInfo.getInt(RedirectConstant.KEY_STATUS);
                List<String> routeIdList = ruleInfo.getList(RedirectConstant.KEY_ROUTES);
                Rule rule = RedirectRuleFactory.getRule(ruleMode, ruleId, ruleStatus, routeIdList, ruleData, ruleAction);
                // 注册规则
                Rules rules= ruleMapping.computeIfAbsent(zoneId, k -> new Rules());
                rules.register(rule);
                Logger.info("Redirect Rule Added: %s", rule);
            } else if (event.matched(WatchEventType.UPDATED)) {
                String ruleId = FileUtil.splitFileName(path);
                String content = new String(data);
                Param ruleInfo = JsonUtil.parseJsonParam(content);
                // 解析动作
                String ruleMode = ruleInfo.getString(RedirectConstant.KEY_MODE);
                Param ruleAction = ruleInfo.getParam(RedirectConstant.KEY_ACTION);
                // 创建规则
                String ruleData = ruleInfo.getString(RedirectConstant.KEY_RULE);
                int ruleStatus = ruleInfo.getInt(RedirectConstant.KEY_STATUS);
                List<String> routeIdList = ruleInfo.getList(RedirectConstant.KEY_ROUTES);
                Rule rule = RedirectRuleFactory.getRule(ruleMode, ruleId, ruleStatus, routeIdList, ruleData, ruleAction);
                // 更新规则
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                Rules rules= ruleMapping.computeIfAbsent(zoneId, k -> new Rules());
                rules.update(rule);
                Logger.info("Redirect Rule Updated: %s", rule);
            } else if (event.matched(WatchEventType.DELETED)) {
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                Rules rules = ruleMapping.computeIfAbsent(zoneId, k -> new Rules());
                String ruleId = FileUtil.splitFileName(path);
                rules.unregister(ruleId);
                Logger.info("Redirect Rule Deleted: %s", ruleId);
            }
        }
    }
}
