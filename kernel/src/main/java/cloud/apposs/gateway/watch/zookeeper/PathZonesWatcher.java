package cloud.apposs.gateway.watch.zookeeper;

import cloud.apposs.gateway.GatewayConstants;
import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.plugin.Plugin;
import cloud.apposs.gateway.plugin.PluginSupport;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.route.RouteParser;
import cloud.apposs.gateway.upstream.Upstream;
import cloud.apposs.gateway.upstream.UpstreamParser;
import cloud.apposs.gateway.util.GatewayUtil;
import cloud.apposs.gateway.watch.IWatch;
import cloud.apposs.gateway.watch.Node;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.gateway.zone.ZoneParser;
import cloud.apposs.logger.Logger;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * /gateway/zones节点下的网域节点变化监听，主要监听网域节点下的路由、上游配置变化
 */
public final class PathZonesWatcher {
    protected final GatewayContext context;

    private final IWatch watcher;

    private final String path;

    private final CuratorFramework zkClient;

    private final ZookeeperPathSupport pathSupport;

    private final Charset charset;

    public PathZonesWatcher(GatewayContext context, IWatch watcher, String path, CuratorFramework zkClient,
                            ZookeeperPathSupport pathSupport, Charset charset) throws Exception {
        this.context = context;
        this.watcher = watcher;
        this.path = GatewayUtil.getZonesPath(path);
        this.zkClient = zkClient;
        this.pathSupport = pathSupport;
        this.charset = charset;
        handleZkPathsInit();
    }

    public void start() throws Exception {
        // 监听/gateway/zones节点下的所有网域节点的变化
        handleZonePathWatch();
        // 遍历/gateway/zones节点所有网域节点，然后监听每个网域节点下的路由、上游配置变化
        List<String> zonePathList = zkClient.getChildren().forPath(path);
        if (zonePathList != null) {
            for (String zonePath : zonePathList) {
                String zoneFullPath = path + "/" + zonePath;
                handleRoutePathWatch(zoneFullPath);
                handleUpstreamPathWatch(zoneFullPath);
                handleSettingPathWatch(zoneFullPath);
            }
        }
    }

    public List<Zone> pullZones() throws Exception {
        // 扫描Zookeeper节点/gateway下的所有网域列表，然后解析节点内容为路由配置
        List<String> zonePathList = zkClient.getChildren().forPath(path);
        if (zonePathList == null) {
            return null;
        }
        List<Zone> zones = new ArrayList<Zone>();
        for (String zonePath : zonePathList) {
            String content = null;
            String settingPath = path + "/" + zonePath + Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_ZONE;
            if (zkClient.checkExists().forPath(settingPath) != null) {
                content = new String(zkClient.getData().forPath(settingPath), charset);
            }
            Zone zone = ZoneParser.parse(zonePath, content);
            zones.add(zone);
            String zoneFullPath = path + "/" + zonePath;
            // 扫描Zookeeper节点/gateway/{zone}/upsreams下的所有上游服务配置
            handleUpstreamInit(zoneFullPath, zone);
            // 扫描Zookeeper节点/gateway/{zone}/routes下的所有路由配置
            handleRouteInit(zoneFullPath, zone);
            // 扫描Zookeeper节点/gateway/{zone}/setting/plugins下的插件列表配置
            handlePluginsInit(zoneFullPath, zone);
            Logger.info("Load Zone '%s' Success", zonePath);
        }
        return zones;
    }

    private void handleZkPathsInit() throws Exception {
        // 先判断根节点是否存在，不存在则创建
        CuratorZookeeperClient zookeeperClient = zkClient.getZookeeperClient();
        zkClient.newNamespaceAwareEnsurePath(path).ensure(zookeeperClient);
        // 遍历path下的所有zone目录，如果不存在子节点routes/upstreams则创建
        List<String> zonePathList = zkClient.getChildren().forPath(path);
        if (zonePathList == null || zonePathList.isEmpty()) {
            // 如果不存在则创建zone.default目录作为默认网域
            zonePathList = new ArrayList<>(Arrays.asList(Zone.DEFAULT_ZONE));
        }
        for (String zonePath : zonePathList) {
            String zoneFullPath = path + "/" + zonePath;
            zkClient.newNamespaceAwareEnsurePath(zoneFullPath).ensure(zookeeperClient);
            zkClient.newNamespaceAwareEnsurePath(zoneFullPath + Node.SETTING.getPath()).ensure(zookeeperClient);
            zkClient.newNamespaceAwareEnsurePath(zoneFullPath + Node.ROUTES.getPath()).ensure(zookeeperClient);
            zkClient.newNamespaceAwareEnsurePath(zoneFullPath + Node.UPSTREAMS.getPath()).ensure(zookeeperClient);
        }
    }

    private void handleZonePathWatch() throws Exception {
        pathSupport.addPathChildrenCache(path, (client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    // 网域节点新增
                    handleZoneAdded(event.getData().getPath());
                    break;
                case CHILD_REMOVED:
                    // 网域节点删除
                    handleZoneRemoved(event.getData().getPath());
                    break;
                default:
                    break;
            }
        });
    }

    private void handleRoutePathWatch(String zonePath) throws Exception {
        pathSupport.addPathChildrenCache(zonePath + Node.ROUTES.getPath(), (client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    handleRouteAdd(event.getData());
                    break;
                case CHILD_UPDATED:
                    handleRouteUpdate(event.getData());
                    break;
                case CHILD_REMOVED:
                    handleRouteRemove(event.getData());
                    break;
                default:
                    break;
            }
        });
    }

    private void handleUpstreamPathWatch(String zonePath) throws Exception {
        pathSupport.addPathChildrenCache(zonePath + Node.UPSTREAMS.getPath(), (client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    handleUpstreamAdd(event.getData());
                    break;
                case CHILD_UPDATED:
                    handleUpstreamUpdate(event.getData());
                    break;
                case CHILD_REMOVED:
                    handleUpstreamRemove(event.getData());
                    break;
                default:
                    break;
            }
        });
    }

    private void handleSettingPathWatch(String zonePath) throws Exception {
        String settingPath = zonePath + Node.SETTING.getPath();
        pathSupport.addPathChildrenCache(settingPath, (client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                case CHILD_UPDATED:
                    handleSettingUpdate(event.getData());
                    break;
                default:
                    break;
            }
        });
    }

    private void handleZoneAdded(String zonePath) throws Exception {
        String zoneName = zonePath.substring(zonePath.lastIndexOf("/") + 1);
        Zone zone = new Zone(zoneName);
        context.getZones().addZone(zone);
        // 自动创建路由、上游配置节点并建立监听
        String zoneRoutePath = zonePath + Node.ROUTES.getPath();
        CuratorZookeeperClient zookeeperClient = zkClient.getZookeeperClient();
        zkClient.newNamespaceAwareEnsurePath(zoneRoutePath).ensure(zookeeperClient);
        String zoneUpstreamPath = zonePath + Node.UPSTREAMS.getPath();
        zkClient.newNamespaceAwareEnsurePath(zoneUpstreamPath).ensure(zookeeperClient);
        // 注册监听和网域插件，包括路由、上游配置节点和插件
        handleRoutePathWatch(zonePath);
        handleUpstreamPathWatch(zonePath);
        for (Plugin plugin : PluginSupport.getCommonPluginList(context)) {
            plugin.registerWatcher(zone, context, watcher);
            zone.addPlugin(plugin);
        }
    }

    private void handleSettingUpdate(ChildData data) throws Exception {
        String zonePath = data.getPath();
        String pathName = zonePath.substring(zonePath.lastIndexOf("/") + 1);
        if (pathName.equals(GatewayConstants.GATEWAY_SEETING_PATH_ZONE)) {
            String zoneId = GatewayUtil.getZoneIdByPath(zonePath);
            byte[] dataBytes = data.getData();
            if (dataBytes == null) {
                return;
            }
            String content = new String(dataBytes, charset);
            Zone parsedZone = ZoneParser.parse(zoneId, content);
            context.getZones().updateZone(zoneId, parsedZone);
        } else if (pathName.equals(GatewayConstants.GATEWAY_SEETING_PATH_PLUGINS)) {
            byte[] dataBytes = data.getData();
            if (dataBytes == null) {
                return;
            }
            String content = new String(dataBytes, charset);
            List<Param> plugins = JsonUtil.parseJsonTable(content);
            if (plugins == null) {
                return;
            }
            String zoneId = GatewayUtil.getZoneIdByPath(zonePath);
            Zone zone = context.getZones().getZone(zoneId);
            for (Param plugin : plugins) {
                String pluginName = plugin.getString(Plugin.KEY_NAME);
                Param pluginMetadata = plugin.getParam(Plugin.KEY_METADATA);
                zone.updatePlugin(pluginName, pluginMetadata);
            }
        }
    }

    private void handleZoneRemoved(String zonePath) throws Exception {
        String zoneId = zonePath.substring(zonePath.lastIndexOf("/") + 1);
        Zone zone = new Zone(zoneId);
        context.getZones().removeZone(zone);
    }

    private boolean handleUpstreamInit(String zoneDir, Zone zone) throws Exception {
        List<String> upstreamPathList = zkClient.getChildren().forPath(zoneDir + Node.UPSTREAMS.getPath());
        for (String upstreamPath : upstreamPathList) {
            byte[] data = zkClient.getData().forPath(zoneDir + Node.UPSTREAMS.getPath() + "/" + upstreamPath);
            String upstreamContent = new String(data, charset);
            Upstream upstream = UpstreamParser.parse(upstreamPath, upstreamContent, context);
            zone.addUpstream(upstream);
        }
        return true;
    }

    private boolean handleRouteInit(String zoneDir, Zone zone) throws Exception {
        List<String> routePathList = zkClient.getChildren().forPath(zoneDir + Node.ROUTES.getPath());
        for (String routePath : routePathList) {
            byte[] data = zkClient.getData().forPath(zoneDir + Node.ROUTES.getPath() + "/" + routePath);
            String routeContent = new String(data, charset);
            Route route = RouteParser.parse(routePath, routeContent, context);
            zone.addRoute(route);
        }
        return true;
    }

    private boolean handlePluginsInit(String zoneDir, Zone zone) throws Exception {
        String pluginPath = zoneDir + Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_PLUGINS;
        // 判断zk path 是否存在
        if (zkClient.checkExists().forPath(pluginPath) == null) {
            return false;
        }
        byte[] data = zkClient.getData().forPath(pluginPath);
        if (data == null) {
            return false;
        }
        String content = new String(data, charset);
        List<Param> plugins = JsonUtil.parseJsonTable(content);
        for (Param plugin : plugins) {
            String pluginName = plugin.getString(Plugin.KEY_NAME);
            Param pluginMetadata = plugin.getParam(Plugin.KEY_METADATA);
            zone.putPluginConfigMapping(pluginName, pluginMetadata);
        }
        return true;
    }

    /**
     * 处理路由配置添加
     *
     * @param  data 路由配置数据
     * @return 是否处理成功
     */
    private boolean handleRouteAdd(ChildData data) throws Exception {
        Route route = parseRouteFile(data);
        if (route == null) {
            return false;
        }
        String zoneId = GatewayUtil.getZoneIdByPath(data.getPath());
        Zone zone = context.getZones().getZone(zoneId);
        zone.addRoute(route);
        return true;
    }

    /**
     * 处理路由配置更新
     *
     * @param  data 路由配置数据
     * @return 是否处理成功
     */
    private boolean handleRouteUpdate(ChildData data) throws Exception {
        Route route = parseRouteFile(data);
        if (route == null) {
            return false;
        }
        String zoneId = GatewayUtil.getZoneIdByPath(data.getPath());
        Zone zone = context.getZones().getZone(zoneId);
        zone.updateRoute(route);
        return true;
    }

    /**
     * 处理路由配置删除
     *
     * @param  data 路由配置数据
     * @return 是否处理成功
     */
    private boolean handleRouteRemove(ChildData data) throws Exception {
        String routePath = data.getPath();
        routePath = routePath.substring(routePath.lastIndexOf("/") + 1);
        String zoneId = GatewayUtil.getZoneIdByPath(data.getPath());
        Zone zone = context.getZones().getZone(zoneId);
        zone.removeRoute(routePath);
        return true;
    }

    /**
     * 处理上游服务添加
     *
     * @param  data 上游服务数据
     * @return 是否处理成功
     */
    private boolean handleUpstreamAdd(ChildData data) throws Exception {
        Upstream upstream = parseUpstreamFile(data);
        if (upstream == null) {
            return false;
        }
        String zoneId = GatewayUtil.getZoneIdByPath(data.getPath());
        Zone zone = context.getZones().getZone(zoneId);
        zone.addUpstream(upstream);
        return true;
    }

    /**
     * 处理上游服务更新
     *
     * @param  data 上游服务数据
     * @return 是否处理成功
     */
    private boolean handleUpstreamUpdate(ChildData data) throws Exception {
        Upstream upstream = parseUpstreamFile(data);
        if (upstream == null) {
            return false;
        }
        String zoneId = GatewayUtil.getZoneIdByPath(data.getPath());
        Zone zone = context.getZones().getZone(zoneId);
        zone.updateUpstream(upstream);
        return true;
    }

    /**
     * 处理上游服务删除
     *
     * @param  data 上游服务数据
     * @return 是否处理成功
     */
    private boolean handleUpstreamRemove(ChildData data) throws Exception {
        String upstreamPath = data.getPath();
        upstreamPath = upstreamPath.substring(upstreamPath.lastIndexOf("/") + 1);
        String zoneId = GatewayUtil.getZoneIdByPath(data.getPath());
        Zone zone = context.getZones().getZone(zoneId);
        zone.removeUpstream(upstreamPath);
        return true;
    }

    private Route parseRouteFile(ChildData data) throws Exception {
        // 截取路由配置的ID，即/gateway/routes/xxx中的xxx
        String routePath = data.getPath();
        routePath = routePath.substring(routePath.lastIndexOf("/") + 1);
        byte[] routeData = data.getData();
        // 解析路由配置
        String routeContent = new String(routeData, charset);
        return RouteParser.parse(routePath, routeContent, context);
    }

    private Upstream parseUpstreamFile(ChildData data) throws Exception {
        // 截取上游配置的ID，即/gateway/upstreams/xxx中的xxx
        String upstreamPath = data.getPath();
        upstreamPath = upstreamPath.substring(upstreamPath.lastIndexOf("/") + 1);
        byte[] upstreamData = data.getData();
        // 解析上游配置
        String upstreamContent = new String(upstreamData, charset);
        return UpstreamParser.parse(upstreamPath, upstreamContent, context);
    }
}
