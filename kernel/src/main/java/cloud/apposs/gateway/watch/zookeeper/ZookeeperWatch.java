package cloud.apposs.gateway.watch.zookeeper;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.watch.AbstractWatch;
import cloud.apposs.gateway.watch.WatchEventType;
import cloud.apposs.gateway.watch.WatchListener;
import cloud.apposs.gateway.zone.Zone;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.nio.charset.Charset;
import java.util.List;

public class ZookeeperWatch extends AbstractWatch {
    public static final int DEAFAULT_ZK_CONNECT_TIMEOUT = 60 * 1000;
    public static final int DEAFAULT_ZK_SESSION_TIMEOUT = 60 * 1000;

    private final CuratorFramework zkClient;

    private final ZookeeperPathSupport pathSupport;

    private final PathZonesWatcher zonesWatcher;

    private final PathGlobalWatcher globalWatcher;

    public ZookeeperWatch(GatewayContext context, String zkServers, int connectTimeout, int sessionTimeout, String path, Charset charset) throws Exception {
        super(context);
        // 创建Zookeeper客户端
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.zkClient = CuratorFrameworkFactory.builder()
                .connectString(zkServers)
                .sessionTimeoutMs(sessionTimeout)
                .connectionTimeoutMs(connectTimeout)
                .retryPolicy(retryPolicy)
                .build();
        this.zkClient.start();
        this.pathSupport = new ZookeeperPathSupport(zkClient);
        this.zonesWatcher = new PathZonesWatcher(context, this, path, zkClient, pathSupport, charset);
        this.globalWatcher = new PathGlobalWatcher(context, this, path, zkClient, pathSupport, charset);
    }

    @Override
    public void start() throws Exception {
        zonesWatcher.start();
        globalWatcher.start();
    }

    @Override
    public List<Zone> pullZones() throws Exception {
        return zonesWatcher.pullZones();
    }

    @Override
    public void addListener(String watchPath, WatchListener watchListener) throws Exception {
        CuratorZookeeperClient zookeeperClient = zkClient.getZookeeperClient();
        zkClient.newNamespaceAwareEnsurePath(watchPath).ensure(zookeeperClient);
        pathSupport.addPathChildrenCache(watchPath, (client, event) -> {
            byte[] data = event.getData().getData();
            switch (event.getType()) {
                case CHILD_ADDED:
                    watchListener.onNodeChanged(event.getData().getPath(), data, WatchEventType.ADDED);
                    break;
                case CHILD_REMOVED:
                    watchListener.onNodeChanged(event.getData().getPath(), data, WatchEventType.DELETED);
                    break;
                case CHILD_UPDATED:
                    watchListener.onNodeChanged(event.getData().getPath(), data, WatchEventType.UPDATED);
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public void close() {
        pathSupport.close();
        zkClient.close();
    }
}
