package cloud.apposs.gateway.watch;

import cloud.apposs.gateway.zone.Zone;

import java.util.List;

/**
 * 配置中心节点监听，由不同的类实现，如Zookeeper/FS/ETCD，节点配置结构如下：
 * <pre>
 * 配置路由：/gateway/{zone}/route
 * 配置插件：/gateway/{zone}/plugin
 * 配置上游：/gateway/{zone}/upstream
 * </pre>
 */
public interface IWatch {
    /**
     * 启动节点监听服务
     */
    void start() throws Exception;

    /**
     * 从注册中心拉取zone所有配置列表，主要应用在网关服务启动时
     */
    List<Zone> pullZones() throws Exception;

    /**
     * 添加配置监听
     *
     * @param watchPath     监听路径，如：/gateway/{zone}/waf
     * @param watchListener 监听器
     */
    void addListener(String watchPath, WatchListener watchListener) throws Exception;

    /**
     * 关闭节点监听服务，主要应用在网关服务关闭时
     */
    void close();
}
