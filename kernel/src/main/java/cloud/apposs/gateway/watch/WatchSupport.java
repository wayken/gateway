package cloud.apposs.gateway.watch;

import cloud.apposs.gateway.GatewayConfig;
import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.watch.fs.FSWatch;
import cloud.apposs.gateway.watch.zookeeper.ZookeeperWatch;
import cloud.apposs.gateway.zone.Zone;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

public final class WatchSupport {
    private final IWatch watch;

    public WatchSupport(GatewayConfig config, GatewayContext context) throws Exception {
        // 根据不同的监听类型启动不同的监听服务
        String watchType = config.getWatchType();
        if (Objects.equals(watchType, GatewayConfig.WATCH_TYPE_ZOOKEEPER)) {
            this.watch = new ZookeeperWatch(context, config.getWatchServers(),
                    ZookeeperWatch.DEAFAULT_ZK_CONNECT_TIMEOUT, ZookeeperWatch.DEAFAULT_ZK_SESSION_TIMEOUT,
                    config.getWatchPath(), Charset.forName(config.getCharset())
            );
        } else if (Objects.equals(watchType, GatewayConfig.WATCH_TYPE_FS)) {
            this.watch = new FSWatch(context, config.getWatchPath());
        } else {
            throw new UnsupportedOperationException("Unsupported service watch type " + config.getWatchType());
        }
    }

    /**
     * 启动节点监听服务
     */
    public WatchSupport start() throws Exception {
        watch.start();
        return this;
    }

    public IWatch getWatch() {
        return watch;
    }

    /**
     * 从注册中心拉取Zone配置列表，主要应用在网关服务启动时
     */
    public List<Zone> pullZones() throws Exception {
        return watch.pullZones();
    }

    /**
     * 添加配置监听
     */
    public void addListener(String watchPath, WatchListener listener) throws Exception {
        watch.addListener(watchPath, listener);
    }

    public void close() {
        watch.close();
    }
}
