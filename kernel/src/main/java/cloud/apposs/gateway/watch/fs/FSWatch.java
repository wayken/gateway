package cloud.apposs.gateway.watch.fs;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.watch.AbstractWatch;
import cloud.apposs.gateway.watch.WatchListener;
import cloud.apposs.gateway.zone.Zone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于文件系统的节点监听服务，主要用于开发环境下的配置监听，各节点配置规范如下：
 * <pre>
 *     配置路由：${filepath}/gateway/zones/{zone}/routes/{routeId}
 *     配置上游：${filepath}/gateway/zones/{zone}/upstreams/{upstreamId}
 * </pre>
 * 其他扩展插件（如Waf、Cache等）的配置规范如下：
 * <pre>
 *     配置插件：${filepath}/gateway/zones/{zone}/waf/xxx
 *     配置插件：${filepath}/gateway/zones/{zone}/cache/xxx
 *     配置插件：${filepath}/gateway/zones/{zone}/rewrite/xxx
 * </pre>
 */
public class FSWatch extends AbstractWatch {
    private final FileZonesWatcher fileZonesWatcher;
    private final FileGlobalWatcher fileGlobalWatcher;

    /** 所有插件的监控器 */
    private final List<PluginMonitor> pluginMonitors = new ArrayList<>();

    public FSWatch(GatewayContext context, String watchDir) throws Exception {
        super(context);
        if (watchDir == null || watchDir.trim().isEmpty()) {
            throw new IllegalArgumentException("workDir");
        }
        this.fileZonesWatcher = new FileZonesWatcher(watchDir, context, this);
        this.fileGlobalWatcher = new FileGlobalWatcher(watchDir, context, this);
    }

    /**
     * 启动节点监听服务
     * 主要是监听文件系统的文件变化，当文件目录下的文件发生变化时会触发监听事件
     */
    @Override
    public void start() throws Exception {
        new Thread(fileZonesWatcher).start();
        new Thread(fileGlobalWatcher).start();
    }

    @Override
    public List<Zone> pullZones() throws Exception {
        return fileZonesWatcher.pullZones();
    }

    @Override
    public void addListener(String watchPath, WatchListener watchListener) throws Exception {
        File rootDir = new File(watchPath);
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
        PluginMonitor monitor = new PluginMonitor(watchPath, watchListener);
        pluginMonitors.add(monitor);
        new Thread(monitor).start();
    }

    @Override
    public void close() {
        if (fileZonesWatcher != null) {
            fileZonesWatcher.close();
        }
        if (fileGlobalWatcher != null) {
            fileGlobalWatcher.close();
        }
        for (PluginMonitor pluginMonitor : pluginMonitors) {
            pluginMonitor.close();
        }
    }
}
