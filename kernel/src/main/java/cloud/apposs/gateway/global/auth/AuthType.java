package cloud.apposs.gateway.global.auth;

/**
 * {@link Auth}认证服务类型，包括
 * <pre>
 * 1. API KEY 认证
 * 2. JWT 认证
 * 3. 鉴权服务转发认证
 * </pre>
 */
public enum AuthType {
    KEY("key"),
    JWT("jwt"),
    SERVER("server"),
    CUSTOM("custom")
    ;
    private final String type;

    AuthType(String type) {
        this.type = type;
    }

    public boolean matched(String type) {
        return type.equals(this.type);
    }
}
