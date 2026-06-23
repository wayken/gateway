package cloud.apposs.gateway.watch;

/**
 * 节点枚举，用于监控节点的变化
 */
public enum Node {
    SETTING("/setting"),
    ROUTES("/routes"),
    UPSTREAMS("/upstreams"),
    GLOBAL_IPS("/ips"),
    GLOBAL_CERTIFICATES("/certificates"),
    GLOBAL_AUTHS("/auths"),
    GLOBAL_PROVIDERS("/providers")
    ;
    private final String path;

    Node(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
