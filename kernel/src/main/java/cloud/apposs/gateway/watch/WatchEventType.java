package cloud.apposs.gateway.watch;

/**
 * 服务节点监听事件类型
 */
public enum WatchEventType {
    /**
     * 节点新增
     */
    ADDED((byte) 1),
    /**
     * 节点删除
     */
    DELETED((byte) 2),
    /**
     * 节点数据变更
     */
    UPDATED((byte) 3)
    ;
    private final byte kind;

    WatchEventType(byte kind) {
        this.kind = kind;
    }

    public boolean matched(WatchEventType event) {
        return event.kind == this.kind;
    }
}
