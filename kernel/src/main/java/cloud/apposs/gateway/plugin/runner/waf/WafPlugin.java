package cloud.apposs.gateway.plugin.runner.waf;

import cloud.apposs.cache.CacheManager;
import cloud.apposs.gateway.GatewayConstants;
import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.annotation.CommonPlugin;
import cloud.apposs.gateway.plugin.PluginAdapter;
import cloud.apposs.gateway.plugin.PluginResult;
import cloud.apposs.gateway.plugin.runner.waf.system.WafSystemRule;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.rules.*;
import cloud.apposs.gateway.rules.mvel.MVELRule;
import cloud.apposs.gateway.util.GatewayUtil;
import cloud.apposs.gateway.watch.IWatch;
import cloud.apposs.gateway.watch.WatchEventType;
import cloud.apposs.gateway.watch.WatchListener;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.logger.Logger;
import cloud.apposs.react.React;
import cloud.apposs.util.FileUtil;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WAF插件，基于自定义规则的防御插件，防止Web应用程序遭受恶意攻击
 * 规则格式如下：
 * <pre>
 * {
 *     "status": 1,
 *     "route": [1, 2],
 *     "rule": "if request.host == 'xxx.com' && request.path = '/xxx/'",
 *     "action": {
 *         "type": "block | jschallenge",
 *         "code": 403,
 *         "content": "Forbidden"
 *     }
 * }
 * </pre>
 */
@CommonPlugin
public class WafPlugin extends PluginAdapter {
    public static final String NAME = "Waf";
    private static final int PLUGIN_PRIORITY = 1070;

    /**
     * 自定义规则集合，用于存储所有的规则，每个Zone对应一个规则集合
     */
    private final Map<String, Rules> ruleMapping = new ConcurrentHashMap<>();

    private final RulesEngine ruleEngine = new DefaultRulesEngine();

    /**
     * 系统规则过滤器，用于过滤规则
     */
    private final WafRuleFilter ruleFilter = new WafRuleFilter();

    @Autowired
    private CacheManager cache;

    public WafPlugin() {
        super(NAME);
    }

    @Override
    public int getPriority() {
        return PLUGIN_PRIORITY;
    }

    @Override
    public void registerWatcher(Zone zone, GatewayContext context, IWatch watcher) throws Exception {
        // 监听自定义规则目录
        String defaultWafRulePath = GatewayUtil.getZonesPath(context.getConfig().getWatchPath()) + "/" + zone.getId() + WafConstant.DEFAULT_WAF_PATH;
        watcher.addListener(defaultWafRulePath, new WafDefaultRuleListener());
        // 监听系统规则目录
        String systemWafRulePath = GatewayUtil.getZonesPath(context.getConfig().getWatchPath()) + "/" + zone.getId() + WafConstant.SYSTEM_WAF_PATH;
        watcher.addListener(systemWafRulePath, new WafSystemRuleListener());
    }

    @Override
    public React<PluginResult> preFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) {
        return React.emitter(() -> {
            Rules rules = ruleMapping.get(zone.getId());
            // 先走自定义规则过滤器
            if (rules != null && !rules.isEmpty()) {
                Facts facts = (Facts) request.getAttribute(GatewayConstants.REQUEST_ATTRIBUTE_RULES_FACTS);
                boolean matched = ruleEngine.fire(rules, facts, route, request, response);
                if (matched) {
                    return PluginResult.SKIP;
                }
            }
            // 再走系统规则过滤器
            if (ruleFilter.match(request, response)) {
                response.write(handleWafHtmlOutput(), true);
                return PluginResult.SKIP;
            }
            return PluginResult.SUCCESS;
        });
    }

    private byte[] handleWafHtmlOutput() throws IOException {
        // 读取当前项目下resources/pages/forbiden.html文件内容返回给客户端
        InputStream pageInputStream = getClass().getClassLoader().getResourceAsStream("pages/forbidden.html");
        if (pageInputStream == null) {
            return null;
        }
        try {
            // 读取文件内容
            byte[] content = new byte[pageInputStream.available()];
            pageInputStream.read(content);
            return content;
        } finally {
            pageInputStream.close();
        }
    }

    private class WafDefaultRuleListener implements WatchListener {
        @Override
        public void onNodeChanged(String path, byte[] data, WatchEventType event) {
            if (event.matched(WatchEventType.ADDED)) {
                String ruleId = FileUtil.splitFileName(path);
                String content = new String(data);
                Param wafRuleInfo = JsonUtil.parseJsonParam(content);
                // 解析动作
                Param ruleAction = wafRuleInfo.getParam(WafConstant.KEY_ACTION);
                String ruleActionType = ruleAction.getString(WafConstant.Action.KEY_TYPE);
                Action action = WafActionFactory.getRuleAction(ruleActionType, ruleAction);
                // 创建规则
                String ruleData = wafRuleInfo.getString(WafConstant.KEY_RULE);
                int ruleStatus = wafRuleInfo.getInt(WafConstant.KEY_STATUS);
                int rulePriority = wafRuleInfo.getInt(WafConstant.KEY_PRIORITY);
                List<String> routeIdList = wafRuleInfo.getList(WafConstant.KEY_ROUTES);
                boolean isActionSkip = WafConstant.Action.isSkip(ruleActionType);
                MVELRule rule = WafRuleFactory.getRule(ruleActionType, ruleId, ruleStatus, rulePriority, routeIdList, isActionSkip, cache).when(ruleData);
                rule.then(action);
                // 注册规则
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                Rules rules= ruleMapping.computeIfAbsent(zoneId, k -> new Rules());
                rules.register(rule);
                Logger.info("WAF Default Rule Added: %s", rule);
            } else if (event.matched(WatchEventType.UPDATED)) {
                String ruleId = FileUtil.splitFileName(path);
                String content = new String(data);
                Param wafRuleInfo = JsonUtil.parseJsonParam(content);
                // 解析动作
                Param ruleAction = wafRuleInfo.getParam(WafConstant.KEY_ACTION);
                String ruleActionType = ruleAction.getString(WafConstant.Action.KEY_TYPE);
                Action action = WafActionFactory.getRuleAction(ruleActionType, ruleAction);
                // 创建规则
                String ruleData = wafRuleInfo.getString(WafConstant.KEY_RULE);
                int ruleStatus = wafRuleInfo.getInt(WafConstant.KEY_STATUS);
                int rulePriority = wafRuleInfo.getInt(WafConstant.KEY_PRIORITY);
                List<String> routeIdList = wafRuleInfo.getList(WafConstant.KEY_ROUTES);
                boolean isActionSkip = WafConstant.Action.isSkip(ruleActionType);
                MVELRule rule = WafRuleFactory.getRule(ruleActionType, ruleId, ruleStatus, rulePriority, routeIdList, isActionSkip, cache).when(ruleData);
                rule.then(action);
                // 更新规则
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                Rules rules= ruleMapping.computeIfAbsent(zoneId, k -> new Rules());
                rules.update(rule);
                Logger.info("WAF Default Rule Updated: %s", rule);
            } else if (event.matched(WatchEventType.DELETED)) {
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                Rules rules = ruleMapping.computeIfAbsent(zoneId, k -> new Rules());
                String ruleId = FileUtil.splitFileName(path);
                rules.unregister(ruleId);
                Logger.info("WAF Default Rule Deleted: %s", ruleId);
            }
        }
    }

    private class WafSystemRuleListener implements WatchListener {
        @Override
        public void onNodeChanged(String path, byte[] data, WatchEventType event) {
            String ruleName = FileUtil.splitFileName(path);
            if (event.matched(WatchEventType.ADDED) || event.matched(WatchEventType.UPDATED)) {
                WafSystemRule wafRule = ruleFilter.getRule(ruleName);
                if (wafRule != null) {
                    String content = new String(data);
                    Param wafRuleInfo = JsonUtil.parseJsonParam(content);
                    String ruleData = wafRuleInfo.getString(WafConstant.KEY_RULE);
                    String ruleId = FileUtil.splitFileName(path);
                    MVELRule rule = new MVELRule(ruleId).when(ruleData);
                    wafRule.setRule(rule);
                }
            } else if (event.matched(WatchEventType.DELETED)) {
                ruleFilter.removeRule(ruleName);
            }
        }
    }
}
