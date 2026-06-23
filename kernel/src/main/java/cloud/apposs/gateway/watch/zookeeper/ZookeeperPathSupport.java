package cloud.apposs.gateway.watch.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Zookeeper路径监听支持，包括
 * <pre>
 * 1. 监听指定路径下的子节点变化
 * 2. 当网关关闭时及时关闭所有Zookeeper路径监听器
 * </pre>
 */
public final class ZookeeperPathSupport {
    private final CuratorFramework zkClient;

    private final List<PathChildrenCache> pathChildrenCacheList = new ArrayList<>();

    public ZookeeperPathSupport(CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }

    public void addPathChildrenCache(String path, PathChildrenCacheListener listener) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, path, true);
        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        pathChildrenCache.getListenable().addListener(listener);
        pathChildrenCacheList.add(pathChildrenCache);
    }

    public void close() {
        for (PathChildrenCache pathChildrenCache : pathChildrenCacheList) {
            try {
                pathChildrenCache.close();
            } catch (Exception ignore) {
            }
        }
    }
}
