package cloud.apposs.gateway.watch.fs;

import cloud.apposs.gateway.GatewayConstants;
import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.plugin.Plugin;
import cloud.apposs.gateway.plugin.PluginSupport;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.route.RouteParser;
import cloud.apposs.gateway.upstream.Upstream;
import cloud.apposs.gateway.upstream.UpstreamParser;
import cloud.apposs.gateway.zone.ZoneParser;
import cloud.apposs.gateway.util.GatewayUtil;
import cloud.apposs.gateway.watch.IWatch;
import cloud.apposs.gateway.watch.Node;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.logger.Logger;
import cloud.apposs.util.FileUtil;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * /gateway/zones节点下的网域节点变化监听，主要监听网域节点下的路由、上游配置变化
 */
public final class FileZonesWatcher implements Runnable {
    private final String watchDir;

    private final GatewayContext context;

    private final IWatch watch;

    private final WatchService watchService;

    private final AtomicBoolean running = new AtomicBoolean(true);

    public FileZonesWatcher(String watchDir, GatewayContext context, FSWatch watch) throws Exception {
        this.watchDir = GatewayUtil.getZonesPath(watchDir);
        Path path = Paths.get(watchDir);
        boolean exists = Files.exists(path);
        if (!exists) {
            Files.createDirectories(path);
        }
        this.context = context;
        this.watch = watch;
        this.watchService = FileSystems.getDefault().newWatchService();
        this.handleFsPathsInit();
    }

    @Override
    public void run() {
        // 遍历所有Zone目录下的routes和upstreams目录，进行监听
        File watchDirFile = new File(watchDir);
        File[] zoneDirs = watchDirFile.listFiles();
        if (zoneDirs == null || zoneDirs.length == 0) {
            return;
        }
        try {
            Paths.get(watchDir).register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            for (File zoneDir : zoneDirs) {
                if (!zoneDir.isDirectory()) {
                    continue;
                }
                String zonePath = zoneDir.getPath();
                handleFileNodeWatch(zonePath);
            }
            Logger.info("Gateway File Watch Service for Path: " + watchDir + " regist success");
            while (running.get()) {
                final WatchKey key = watchService.take();
                try {
                    handleWatchEvent(key);
                } catch (Exception e) {
                    // 有可能配置解析有异常，输出错误后继续监听
                    Logger.warn(e, "Gateway File Watch Service for Path: " + watchDir + " Error");
                }
                boolean valid = key.reset();
                if (!valid) {
                    Logger.warn("Gateway File Watch Service for Path: " + watchDir + " key is invalid");
                }
            }
        } catch (ClosedWatchServiceException e) {
            if (!running.get()) {
                Logger.info("Gateway File Watch Service for Path: " + watchDir + " closed");
            } else {
                Logger.error(e, "Gateway File Watch Service for Path: " + watchDir + " watching closed");
            }
        } catch (Exception e) {
            Logger.error(e, "Gateway File Watch Service for Path: " + watchDir + " watching failed");
        }
    }

    public List<Zone> pullZones() throws Exception {
        // 扫描文件目录下的所有文件，然后解析文件内容为路由配置
        File watchDirFile = new File(watchDir);
        if (!watchDirFile.exists()) {
            return null;
        }
        File[] zoneDirs = watchDirFile.listFiles();
        if (zoneDirs == null || zoneDirs.length == 0) {
            return null;
        }
        List<Zone> zones = new ArrayList<Zone>();
        for (int i = 0; i < zoneDirs.length; i++) {
            File zoneDir = zoneDirs[i];
            if (!zoneDir.isDirectory()) {
                continue;
            }
            String zoneId = zoneDir.getName();
            String content = null;
            File settingFile = new File(zoneDir, Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_ZONE);
            if (settingFile.exists()) {
                content = FileUtil.readString(settingFile);
            }
            Zone zone = ZoneParser.parse(zoneId, content);
            zones.add(zone);
            // 读取 `${watchDir}/{zone}/upstreams` 目录下的所有文件，每个文件对应一个上游配置
            File upstreamsDir = new File(zoneDir.getPath() + Node.UPSTREAMS.getPath());
            handleUpstreamInit(upstreamsDir, zone);
            // 读取 `${watchDir}/{zone}/routes` 目录下的所有文件，每个文件对应一个路由配置
            File routesDir = new File(zoneDir.getPath() + Node.ROUTES.getPath());
            handleRouteInit(routesDir, zone);
            // 读取 `${watchDir}/{zone}/setting/plugins` 目录下的插件列表配置文件
            File pluginsFile = new File(zoneDir.getPath() + Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_PLUGINS);
            handlePluginListInit(pluginsFile, zone);

            Logger.info("Load Zone '%s' Success", zoneId);
        }
        return zones;
    }

    private void handleFsPathsInit() {
        // 初始化Fs节点，如果不存在/gateway/{zone}/routes和/gateway/{zone}/upstreams则创建
        File rootDir = new File(watchDir);
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
        // 获取rootDir下的zone目录，如果不存在则创建zone.default目录
        File[] zoneDirs = rootDir.listFiles();
        if (zoneDirs == null || zoneDirs.length == 0) {
            File zoneDir = new File(watchDir + File.separator + Zone.DEFAULT_ZONE);
            zoneDir.mkdirs();
            zoneDirs = rootDir.listFiles();
        }
        for (File zoneDir : zoneDirs) {
            if (!zoneDir.isDirectory()) {
                continue;
            }
            // 获取zone目录下的setting、routes和upstreams目录，如果不存在则创建
            File settingDir = new File(zoneDir.getPath() + Node.SETTING.getPath());
            if (!settingDir.exists()) {
                settingDir.mkdirs();
            }
            File routesDir = new File(zoneDir.getPath() + Node.ROUTES.getPath());
            if (!routesDir.exists()) {
                routesDir.mkdirs();
            }
            File upstreamsDir = new File(zoneDir.getPath() + Node.UPSTREAMS.getPath());
            if (!upstreamsDir.exists()) {
                upstreamsDir.mkdirs();
            }
        }
    }

    private boolean handleUpstreamInit(File upstreamsDir, Zone zone) throws Exception {
        if (!upstreamsDir.exists()) {
            return false;
        }
        File[] upstreamFiles = upstreamsDir.listFiles();
        if (upstreamFiles == null || upstreamFiles.length == 0) {
            return false;
        }
        for (File upstreamFile : upstreamFiles) {
            String upstreamId = upstreamFile.getName();
            // 解析路由配置，文件路由配置格式为JSON数据
            String upstreamContent = FileUtil.readString(upstreamFile);
            Upstream upstream = UpstreamParser.parse(upstreamId, upstreamContent, context);
            if (upstream != null) {
                zone.addUpstream(upstream);
            }
        }
        return true;
    }

    private boolean handleRouteInit(File routesDir, Zone zone) throws Exception {
        if (!routesDir.exists()) {
            return false;
        }
        File[] routeFiles = routesDir.listFiles();
        if (routeFiles == null || routeFiles.length == 0) {
            return false;
        }
        for (File routeFile : routeFiles) {
            String routeId = routeFile.getName();
            // 解析路由配置，文件路由配置格式为JSON数据
            String routeContent = FileUtil.readString(routeFile);
            Route route = RouteParser.parse(routeId, routeContent, context);
            zone.addRoute(route);
        }
        return true;
    }

    private boolean handlePluginListInit(File pluginsFile, Zone zone) throws Exception {
        if (!pluginsFile.exists()) {
            return false;
        }
        String content = FileUtil.readString(pluginsFile);
        Table<Param> plugins = JsonUtil.parseJsonTable(content);
        if (plugins == null) {
            return false;
        }
        for (Param plugin : plugins) {
            String pluginName = plugin.getString(Plugin.KEY_NAME);
            Param pluginMetadata = plugin.getParam(Plugin.KEY_METADATA);
            zone.putPluginConfigMapping(pluginName, pluginMetadata);
        }
        return true;
    }

    /**
     * 处理文件监听事件
     */
    private void handleWatchEvent(WatchKey key) throws Exception {
        for (WatchEvent<?> watchEvent : key.pollEvents()) {
            final WatchEvent.Kind<?> kind = watchEvent.kind();
            if (kind == OVERFLOW) {
                continue;
            }
            if (kind == ENTRY_CREATE) {
                Path watchable = (Path) key.watchable();
                Path path = (Path) watchEvent.context();
                String fullPath = watchable + "/" + path;
                // 判断是否在网域节点下的创建，如果是则进行监听，否则代表是路由或者上游节点的创建
                if (watchable.equals(Paths.get(watchDir))) {
                    handleFileNodeWatch(fullPath);
                    handleZoneAdd(path);
                } else {
                    String routeName = Node.ROUTES.getPath().replace("/", "");
                    String upstreamName = Node.UPSTREAMS.getPath().replace("/", "");
                    String settingName = Node.SETTING.getPath().replace("/", "");
                    if (watchable.endsWith(routeName)) {
                        handleRouteAdd(watchable, path);
                    } else if (watchable.endsWith(upstreamName)) {
                        handleUpstreamAdd(watchable, path);
                    } else if (watchable.endsWith(settingName)) {
                        handleSettingUpdate(watchable, path);
                    }
                }
            } else if (kind == ENTRY_MODIFY) {
                Path watchable = (Path) key.watchable();
                Path path = (Path) watchEvent.context();
                String routeName = Node.ROUTES.getPath().replace("/", "");
                String upstreamName = Node.UPSTREAMS.getPath().replace("/", "");
                String settingName = Node.SETTING.getPath().replace("/", "");
                if (watchable.endsWith(routeName)) {
                    handleRouteUpdate(watchable, path);
                } else if (watchable.endsWith(upstreamName)) {
                    handleUpstreamUpdate(watchable, path);
                } else if (watchable.endsWith(settingName)) {
                    handleSettingUpdate(watchable, path);
                }
            } else if (kind == ENTRY_DELETE) {
                Path watchable = (Path) key.watchable();
                Path path = (Path) watchEvent.context();
                String routeName = Node.ROUTES.getPath().replace("/", "");
                String upstreamName = Node.UPSTREAMS.getPath().replace("/", "");
                if (watchable.endsWith(routeName)) {
                    handleRouteRemove(watchable, path);
                } else if (watchable.endsWith(upstreamName)) {
                    handleUpstreamRemove(watchable, path);
                }
            }
        }
    }

    /**
     * 处理每个网域节点下的routes和upstreams目录监听，
     * 如果不存在对应节点目录则自动创建，然后进行监听
     *
     * @param zone 网域节点目录，如：/data/gateway/xxx.com
     */
    private void handleFileNodeWatch(String zone) throws IOException {
        Path settingPath = Paths.get(zone + Node.SETTING.getPath());
        Path watchRoutesPath = Paths.get(zone + Node.ROUTES.getPath());
        Path watchUpstreamsPath = Paths.get(zone + Node.UPSTREAMS.getPath());
        if (!Files.exists(settingPath)) {
            Files.createDirectories(settingPath);
        }
        if (!Files.exists(watchRoutesPath)) {
            Files.createDirectories(watchRoutesPath);
        }
        if (!Files.exists(watchUpstreamsPath)) {
            Files.createDirectories(watchUpstreamsPath);
        }
        settingPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        watchRoutesPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        watchUpstreamsPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    /**
     * 处理网域节点的新增事件
     */
    private void handleZoneAdd(Path path) throws Exception {
        String zoneName = path.toString();
        Zone zone = new Zone(zoneName);
        context.getZones().addZone(zone);
        // 注册监听和网域插件，包括路由、上游配置节点和插件
        handleFileNodeWatch(path.toString());
        for (Plugin plugin : PluginSupport.getCommonPluginList(context)) {
            plugin.registerWatcher(zone, context, watch);
            zone.addPlugin(plugin);
        }
    }

    /**
     * 处理路由文件新增
     *
     * @param  rootPath  路由文件根目录
     * @param  routeFile 新增的路由文件
     * @return 是否处理成功
     */
    private boolean handleRouteAdd(Path rootPath, Path routeFile) throws Exception {
        Route route = parseRouteFile(rootPath, routeFile);
        if (route == null) {
            return false;
        }
        // 截取路径中的网域名称
        String zoneName = rootPath.getParent().getFileName().toString();
        Zone zone = context.getZones().getZone(zoneName);
        zone.addRoute(route);
        return true;
    }

    /**
     * 处理路由文件变更
     *
     * @param  rootPath  路由文件根目录
     * @param  routeFile 内容变更后的路由文件
     * @return 是否处理成功
     */
    private boolean handleRouteUpdate(Path rootPath, Path routeFile) throws Exception {
        Route route = parseRouteFile(rootPath, routeFile);
        if (route == null) {
            return false;
        }
        // 截取路径中的网域名称
        String zoneName = rootPath.getParent().getFileName().toString();
        Zone zone = context.getZones().getZone(zoneName);
        zone.updateRoute(route);
        return true;
    }

    /**
     * 处理路由文件被删除
     *
     * @param  rootPath  路由文件根目录
     * @param  routeFile 被删除的路由文件
     * @return 是否处理成功
     */
    private boolean handleRouteRemove(Path rootPath, Path routeFile) throws Exception {
        String routeId = routeFile.toString();
        // 截取路径中的网域名称
        String zoneName = rootPath.getParent().getFileName().toString();
        Zone zone = context.getZones().getZone(zoneName);
        zone.removeRoute(routeId);
        return true;
    }

    /**
     * 处理上游文件新增
     *
     * @param  rootPath    上游文件根目录
     * @param  upstreamFile 新增的上游文件
     * @return 是否处理成功
     */
    private boolean handleUpstreamAdd(Path rootPath, Path upstreamFile) throws Exception {
        Upstream upstream = parseUpstreamFile(rootPath, upstreamFile);
        if (upstream == null) {
            return false;
        }
        // 截取路径中的网域名称
        String zoneName = rootPath.getParent().getFileName().toString();
        Zone zone = context.getZones().getZone(zoneName);
        zone.addUpstream(upstream);
        return true;
    }

    /**
     * 处理上游文件变更
     *
     * @param  rootPath     上游文件根目录
     * @param  upstreamFile 内容变更后的上游文件
     * @return 是否处理成功
     */
    public boolean handleUpstreamUpdate(Path rootPath, Path upstreamFile) throws Exception {
        Upstream upstream = parseUpstreamFile(rootPath, upstreamFile);
        if (upstream == null) {
            return false;
        }
        // 截取路径中的网域名称
        String zoneName = rootPath.getParent().getFileName().toString();
        Zone zone = context.getZones().getZone(zoneName);
        zone.updateUpstream(upstream);
        return true;
    }

    public boolean handleSettingUpdate(Path rootPath, Path settingFile) throws Exception {
        String pathName = settingFile.getFileName().toString();
        if (pathName.equals(GatewayConstants.GATEWAY_SEETING_PATH_ZONE)) {
            String zoneId = rootPath.getParent().getFileName().toString();
            Zone parsedZone = parseZoneFile(rootPath, settingFile);
            if (parsedZone == null) {
                return false;
            }
            context.getZones().updateZone(zoneId, parsedZone);
        } else if (pathName.equals(GatewayConstants.GATEWAY_SEETING_PATH_PLUGINS)) {
            List<Param> plugins = parsePluginFile(rootPath, settingFile);
            if (plugins == null) {
                return false;
            }
            String zoneId = rootPath.getParent().getFileName().toString();
            Zone zone = context.getZones().getZone(zoneId);
            for (Param plugin : plugins) {
                String pluginName = plugin.getString(Plugin.KEY_NAME);
                Param pluginMetadata = plugin.getParam(Plugin.KEY_METADATA);
                zone.updatePlugin(pluginName, pluginMetadata);
            }
        }
        return true;
    }

    /**
     * 处理上游服务文件被删除
     *
     * @param  rootPath     上游服务文件根目录
     * @param  upstreamFile 被删除的上游服务文件
     * @return 是否处理成功
     */
    private boolean handleUpstreamRemove(Path rootPath, Path upstreamFile) throws Exception {
        String upstreamId = upstreamFile.toString();
        // 截取路径中的网域名称
        String zoneName = rootPath.getParent().getFileName().toString();
        Zone zone = context.getZones().getZone(zoneName);
        zone.removeUpstream(upstreamId);
        return true;
    }

    private Route parseRouteFile(Path rootPath, Path routeFile) throws Exception {
        String routeId = routeFile.toString();
        Path filePath = rootPath.resolve(routeId);
        if (!waitForFileExistence(filePath, 4000)) {
            return null;
        }
        String routeContent = FileUtil.readString(new File(filePath.toString()));
        return RouteParser.parse(routeId, routeContent, context);
    }

    private Upstream parseUpstreamFile(Path rootPath, Path upstreamFile) throws Exception {
        String upstreamId = upstreamFile.toString();
        Path filePath = rootPath.resolve(upstreamId);
        if (!waitForFileExistence(filePath, 4000)) {
            return null;
        }
        String upstreamContent = FileUtil.readString(new File(filePath.toString()));
        return UpstreamParser.parse(upstreamId, upstreamContent, context);
    }

    private Zone parseZoneFile(Path rootPath, Path zoneFile) throws Exception {
        String zoneName = zoneFile.toString();
        Path filePath = rootPath.resolve(zoneName);
        if (!waitForFileExistence(filePath, 4000)) {
            return null;
        }
        String zoneContent = FileUtil.readString(new File(filePath.toString()));
        return ZoneParser.parse(zoneName, zoneContent);
    }

    private List<Param> parsePluginFile(Path rootPath, Path pluginFile) throws Exception {
        String paramsName = pluginFile.toString();
        Path filePath = rootPath.resolve(paramsName);
        if (!waitForFileExistence(filePath, 4000)) {
            return null;
        }
        String pluginContent = FileUtil.readString(new File(filePath.toString()));
        return JsonUtil.parseJsonTable(pluginContent);
    }

    /**
     * WatchServer监听到文件添加时，可能是文件正在写入，需要等待文件写入完成
     */
    private boolean waitForFileExistence(Path file, long timeout) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout) {
            if (Files.exists(file)) {
                return true;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return false;
    }

    public void close() {
        try {
            running.set(false);
            watchService.close();
        } catch (Exception e) {
            Logger.error(e, "Gateway File Watch Service for Path: " + watchDir + " close failed");
        }
    }
}
