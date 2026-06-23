package cloud.apposs.gateway.plugin.runner.limit;

import cloud.apposs.cache.CacheManager;
import cloud.apposs.gateway.GatewayConstants;
import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.annotation.CommonPlugin;
import cloud.apposs.gateway.plugin.PluginAdapter;
import cloud.apposs.gateway.plugin.PluginResult;
import cloud.apposs.gateway.plugin.runner.limit.rule.LimitRule;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.rules.*;
import cloud.apposs.gateway.util.GatewayUtil;
import cloud.apposs.gateway.watch.IWatch;
import cloud.apposs.gateway.watch.WatchEventType;
import cloud.apposs.gateway.watch.WatchListener;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.guard.exception.BlockException;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.logger.Logger;
import cloud.apposs.react.React;
import cloud.apposs.util.FileUtil;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 速率限制插件，用于限制请求的速率，防止恶意请求，
 * 其中的Resource Key是以RouteId为单元，扩展自定义参数限流规则
 * 规则格式如下：
 * /gateway/xxx.com/limit/{id}
 * <pre>
 * {
 *     "name": "限流规则名称",
 *     "status": 1,
 *     "route": [1, 2],
 *     "rule": "http.path == '/'",
 *     "rate": 100,
 *     "resource": "http.path",
 *     "action": {
 *       "type": "block | jschallenge",
 *       "parameter": {
 *          "code": 403,
 *          "content": "Forbidden"
 *        }
 *      }
 * }
 * </pre>
 */
@CommonPlugin
public class LimitPlugin extends PluginAdapter {
    public static final String NAME = "Limit";
    private static final int PLUGIN_PRIORITY = 1080;

    /**
     * 自定义规则集合，用于存储所有的规则，每个Zone对应一个规则集合
     */
    private final Map<String, Rules> ruleMapping = new ConcurrentHashMap<>();

    private final RulesEngine ruleEngine = new DefaultRulesEngine();

    @Autowired
    private CacheManager cache;

    public LimitPlugin() {
        super(NAME);
    }

    @Override
    public int getPriority() {
        return PLUGIN_PRIORITY;
    }

    @Override
    public void registerWatcher(Zone zone, GatewayContext context, IWatch watcher) throws Exception {
        // 监听规则目录
        String defaultWafRulePath = GatewayUtil.getZonesPath(context.getConfig().getWatchPath()) + "/" + zone.getId() + LimitConstant.DEFAULT_LIMIT_PATH;
        watcher.addListener(defaultWafRulePath, new LimitRuleListener());
    }

    @Override
    public React<PluginResult> preFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) {
        return React.emitter(() -> {
            Rules rules = ruleMapping.get(zone.getId());
            if (rules == null || rules.isEmpty()) {
                return PluginResult.SUCCESS;
            }
            Facts facts = (Facts) request.getAttribute(GatewayConstants.REQUEST_ATTRIBUTE_RULES_FACTS);
            try {
                ruleEngine.fire(rules, facts, zone, route, request, response);
            } catch (BlockException e) {
                return PluginResult.SKIP;
            }
            return PluginResult.SUCCESS;
        });
    }

    private class LimitRuleListener implements WatchListener {
        @Override
        public void onNodeChanged(String path, byte[] data, WatchEventType event) {
            if (event.matched(WatchEventType.ADDED)) {
                String id = FileUtil.splitFileName(path);
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                String content = new String(data);
                Param ruleInfo = JsonUtil.parseJsonParam(content);
                // 解析动作
                Param ruleAction = ruleInfo.getParam(LimitConstant.KEY_ACTION);
                String ruleActionType = ruleAction.getString(LimitConstant.Action.KEY_TYPE);
                int ruleActionDuration = ruleAction.getInt(LimitConstant.Action.KEY_DURATION);
                String ruleActionDurationUnit = ruleAction.getString(LimitConstant.Action.KEY_DURATION_UNIT);
                Action action = LimitActionFactory.getRuleAction(ruleActionType, ruleAction);
                // 创建规则
                String ruleData = ruleInfo.getString(LimitConstant.KEY_RULE);
                int ruleStatus = ruleInfo.getInt(LimitConstant.KEY_STATUS);
                int rulePriority = ruleInfo.getInt(LimitConstant.KEY_PRIORITY);
                List<Param> ruleResources = ruleInfo.getList(LimitConstant.KEY_RESOURCES);
                List<String> routeIdList = ruleInfo.getList(LimitConstant.KEY_ROUTES);
                boolean isActionSkip = LimitConstant.Action.isSkip(ruleActionType);
                int ruleBurst = ruleInfo.getInt(LimitConstant.KEY_BURST);
                String ruleBurstUnit = ruleInfo.getString(LimitConstant.KEY_BURST_UNIT);
                LimitRule rule = LimitRuleFactory.getRule(ruleActionType, id, ruleStatus, rulePriority, routeIdList, zoneId, ruleBurst, ruleBurstUnit, cache);
                rule.when(ruleData);
                rule.setLimitKeys(ruleResources);
                rule.setSkippable(isActionSkip);
                rule.setDuration(ruleActionDuration);
                rule.setDurationUnit(ruleActionDurationUnit);
                rule.then(action);
                // 注册规则
                Rules rules= ruleMapping.computeIfAbsent(zoneId, k -> new Rules());
                rules.register(rule);
                Logger.info("Limit Rule Added: %s", rule);
            } else if (event.matched(WatchEventType.UPDATED)) {
                String id = FileUtil.splitFileName(path);
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                String content = new String(data);
                Param ruleInfo = JsonUtil.parseJsonParam(content);
                // 解析动作
                Param ruleAction = ruleInfo.getParam(LimitConstant.KEY_ACTION);
                String ruleActionType = ruleAction.getString(LimitConstant.Action.KEY_TYPE);
                int ruleActionDuration = ruleAction.getInt(LimitConstant.Action.KEY_DURATION);
                String ruleActionDurationUnit = ruleAction.getString(LimitConstant.Action.KEY_DURATION_UNIT);
                Action action = LimitActionFactory.getRuleAction(ruleActionType, ruleAction);
                // 创建规则
                String ruleData = ruleInfo.getString(LimitConstant.KEY_RULE);
                int ruleStatus = ruleInfo.getInt(LimitConstant.KEY_STATUS);
                int rulePriority = ruleInfo.getInt(LimitConstant.KEY_PRIORITY);
                List<Param> ruleResources = ruleInfo.getList(LimitConstant.KEY_RESOURCES);
                List<String> routeIdList = ruleInfo.getList(LimitConstant.KEY_ROUTES);
                boolean isActionSkip = LimitConstant.Action.isSkip(ruleActionType);
                int ruleBurst = ruleInfo.getInt(LimitConstant.KEY_BURST);
                String ruleBurstUnit = ruleInfo.getString(LimitConstant.KEY_BURST_UNIT);
                LimitRule rule = LimitRuleFactory.getRule(ruleActionType, id, ruleStatus, rulePriority, routeIdList, zoneId, ruleBurst, ruleBurstUnit, cache);
                rule.when(ruleData);
                rule.setLimitKeys(ruleResources);
                rule.setSkippable(isActionSkip);
                rule.setDuration(ruleActionDuration);
                rule.setDurationUnit(ruleActionDurationUnit);
                rule.then(action);
                // 更新规则
                Rules rules = ruleMapping.computeIfAbsent(zoneId, k -> new Rules());
                rules.update(rule);
                Logger.info("Limit Rule Updated: %s", rule);
            } else if (event.matched(WatchEventType.DELETED)) {
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                Rules rules = ruleMapping.computeIfAbsent(zoneId, k -> new Rules());
                String ruleId = FileUtil.splitFileName(path);
                rules.unregister(ruleId);
                Logger.info("Limit Rule Deleted: %s", ruleId);
            }
        }
    }
}
