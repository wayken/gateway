package cloud.apposs.gateway.plugin.runner.cache;

import cloud.apposs.gateway.*;
import cloud.apposs.gateway.annotation.CommonPlugin;
import cloud.apposs.gateway.plugin.PluginAdapter;
import cloud.apposs.gateway.plugin.PluginResult;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.rules.*;
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 网关缓存插件，负责对请求进行缓存，
 * 规则格式如下：
 * <pre>
 * {
 *     "status": 1,
 *     "route": [1, 2],
 *     "rule": "if request.host == 'xxx.com' && request.path = '/xxx/'",
 *     "action": {
 *         "type": "cache-control|cache-force",
 *         "edge": 0,
 *         "ttl": 60
 *     },
 *     "cacheKey": {
 *          "ignoreQuery": true
 *     }
 * }
 * </pre>
 */
@CommonPlugin
public class CachePlugin extends PluginAdapter {
    public static final String NAME = "Cache";
    private static final int PLUGIN_PRIORITY = 1100;

    private final GatewayConfig config;

    /**
     * 自定义规则集合，用于存储所有的规则，每个Zone对应一个规则集合
     */
    private final Map<String, Rules> ruleMapping = new ConcurrentHashMap<>();

    private final RulesEngine ruleEngine = new DefaultRulesEngine();

    public CachePlugin(GatewayConfig config) {
        super(NAME);
        this.config = config;
    }

    @Override
    public int getPriority() {
        return PLUGIN_PRIORITY;
    }

    @Override
    public void registerWatcher(Zone zone, GatewayContext context, IWatch watcher) throws Exception {
        // 监听规则目录
        String cacheRulePath = GatewayUtil.getZonesPath(context.getConfig().getWatchPath()) + "/" + zone.getId() + CacheConstant.DEFAULT_CACHE_PATH;
        watcher.addListener(cacheRulePath, new CacheRuleListener(config));
    }

    /**
     * 请求转发前的拦截，负责判断是否需要缓存，缓存数据是否存在，存在则直接返回不做路由转发
     */
    @Override
    public React<PluginResult> preFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) {
        return React.emitter(() -> {
            Rules rules = ruleMapping.get(zone.getId());
            if (rules != null && !rules.isEmpty()) {
                Facts facts = (Facts) request.getAttribute(GatewayConstants.REQUEST_ATTRIBUTE_RULES_FACTS);
                boolean matched = ruleEngine.fire(rules, facts, route, request, response);
                if (matched) {
                    return PluginResult.SKIP;
                }
            }
            return PluginResult.SUCCESS;
        });
    }

    /**
     * 请求转发后的拦截，负责判断是否需要缓存，并缓存请求后的数据
     */
    @Override
    public void postFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route, Object value) throws Exception {
        Object isCacheable = request.getAttribute(CacheConstant.REQUEST_ATTRIBUTE_CACHEABLE);
        if (Objects.nonNull(isCacheable)) {
            response.putHeader(CacheConstant.HEADER_CACHE_STATUS, CacheConstant.CacheStatus.MISS);
            String dataSourcePath = config.getDataSourcePath();
            String host = request.getRemoteHost();
            String url = request.getUrl();
            String method = request.getMethod();
            Path dataPath = Paths.get(CacheConstant.getCachePath(dataSourcePath, host, url, method));
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
            String headerPath = CacheConstant.getCacheHeaderPath(dataPath.toString());
            String contentPath = CacheConstant.getCacheContentPath(dataPath.toString());
            File headerFile = new File(headerPath);
            File contentFile = new File(contentPath);
            Map<String, String> headers = request.getHeaders();
            StringBuilder headerCotent = new StringBuilder();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(CacheConstant.HEADER_CACHE_STATUS)) {
                    continue;
                }
                headerCotent.append(entry.getKey() + ":" + entry.getValue() + "\r\n");
            }
            headerCotent.append("Content-Type" + ":" + response.getContentType());
            FileUtil.write(headerCotent + "\r\n\r\n", headerFile, false);
            if (value instanceof String) {
                FileUtil.write((String) value, contentFile, false);
            }
        }
    }

    private class CacheRuleListener implements WatchListener {
        private GatewayConfig config;

        public CacheRuleListener(GatewayConfig config) {
            this.config = config;
        }

        @Override
        public void onNodeChanged(String path, byte[] data, WatchEventType event) {
            if (event.matched(WatchEventType.ADDED)) {
                String ruleId = FileUtil.splitFileName(path);
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                String content = new String(data);
                Param ruleInfo = JsonUtil.parseJsonParam(content);
                // 解析动作
                Param ruleAction = ruleInfo.getParam(CacheConstant.KEY_ACTION);
                String ruleActionType = ruleAction.getString(CacheConstant.Action.KEY_TYPE);
                Action action = CacheActionFactory.getRuleAction(ruleActionType, config, ruleAction);
                // 创建规则
                String ruleData = ruleInfo.getString(CacheConstant.KEY_RULE);
                int ruleStatus = ruleInfo.getInt(CacheConstant.KEY_STATUS);
                List<String> routeIdList = ruleInfo.getList(CacheConstant.KEY_ROUTES);
                String dataSourcePath = config.getDataSourcePath();
                MVELRule rule = new CacheRule(ruleId, ruleStatus, routeIdList, dataSourcePath).when(ruleData);
                rule.then(action);
                // 注册规则
                Rules rules= ruleMapping.computeIfAbsent(zoneId, k -> new Rules());
                rules.register(rule);
                Logger.info("Cache Rule Added: %s", rule);
            } else if (event.matched(WatchEventType.UPDATED)) {
                String ruleId = FileUtil.splitFileName(path);
                String content = new String(data);
                Param ruleInfo = JsonUtil.parseJsonParam(content);
                // 解析动作
                Param ruleAction = ruleInfo.getParam(CacheConstant.KEY_ACTION);
                String ruleActionType = ruleAction.getString(CacheConstant.Action.KEY_TYPE);
                Action action = CacheActionFactory.getRuleAction(ruleActionType, config, ruleAction);
                // 创建规则
                String ruleData = ruleInfo.getString(CacheConstant.KEY_RULE);
                int ruleStatus = ruleInfo.getInt(CacheConstant.KEY_STATUS);
                List<String> routeIdList = ruleInfo.getList(CacheConstant.KEY_ROUTES);
                String dataSourcePath = config.getDataSourcePath();
                MVELRule rule = new CacheRule(ruleId, ruleStatus, routeIdList, dataSourcePath).when(ruleData);
                rule.then(action);
                // 更新规则
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                Rules rules= ruleMapping.computeIfAbsent(zoneId, k -> new Rules());
                rules.update(rule);
                Logger.info("Cache Default Rule Updated: %s", rule);
            } else if (event.matched(WatchEventType.DELETED)) {
                String zoneId = GatewayUtil.getZoneIdByPath(path);
                Rules rules = ruleMapping.computeIfAbsent(zoneId, k -> new Rules());
                String ruleId = FileUtil.splitFileName(path);
                rules.unregister(ruleId);
                Logger.info("Cache Default Rule Deleted: %s", ruleId);
            }
        }
    }
}
