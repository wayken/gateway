package cloud.apposs.gateway.upstream;

/**
 * {@link Upstream}上游转发类型枚举，包括
 * <pre>
 * 1. 节点转发
 * 2. DNS域名转发
 * 3. 服务发现转发
 * 4. 静态文本返回
 * 5. 自定义上游转发，需要业务实现{@link Upstream}，并交类注册到{@link cloud.apposs.gateway.GatewayContext}中
 * </pre>
 */
public enum UpstreamType {
    NODE("node"),
    DNS("dns"),
    SERVICE("service"),
    INDEX("index"),
    ECHO("echo"),
    AI("ai"),
    CUSTOM("custom")
    ;
    private final String type;

    UpstreamType(String type) {
        this.type = type;
    }

    public boolean matched(String type) {
        return type.equals(this.type);
    }
}
