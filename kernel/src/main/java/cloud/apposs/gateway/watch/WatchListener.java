package cloud.apposs.gateway.watch;

/**
 * 服务节点监听器，用于监听服务节点的变化，如网域、路由、上游、Waf规则、限流规则等
 */
public interface WatchListener {
    /**
     * 节点配置变化回调
     *
     * @param path  节点路径
     * @param data  节点数据
     * @param event 节点事件类型
     */
    void onNodeChanged(String path, byte[] data, WatchEventType event);
}
