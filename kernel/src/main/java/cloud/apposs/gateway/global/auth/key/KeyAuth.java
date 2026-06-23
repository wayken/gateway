package cloud.apposs.gateway.global.auth.key;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.global.auth.AbstractAuth;
import cloud.apposs.gateway.global.auth.AuthType;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.react.React;
import cloud.apposs.util.Param;

/**
 * 基于key进行权限认证，配置示例如下：
 * <pre>
 * {
 *     "type": "key",
 *     # 认证密钥
 *     "credential": "sk-xxxxxxxxxxxx",
 *     # 认证密钥的来源，请求头或者请求参数
 *     "source": "header | query",
 *     # 认证密钥的参数获取名称
 *     "param": "Authorization",
 * }
 * </pre>
 */
public class KeyAuth extends AbstractAuth {
    public static final String KEY_CREDENTIAL = "credential";
    public static final String KEY_SOURCE = "source";
    public static final String KEY_PARAM = "param";

    private String credential;

    private boolean header = true;

    private String param = "Authorization";

    public KeyAuth(String id, String name, GatewayContext context, Param configuration) {
        super(id, AuthType.KEY.name(), name, context, configuration);
        if (!configuration.containsKey(KEY_CREDENTIAL)) {
            throw new IllegalArgumentException("KeyAuth parameter credential must not be null");
        }
        this.credential = configuration.getString(KEY_CREDENTIAL);
        if (configuration.containsKey(KEY_SOURCE)) {
            this.header = "header".equalsIgnoreCase(configuration.getString(KEY_SOURCE));
        }
        if (configuration.containsKey(KEY_PARAM)) {
            this.param = configuration.getString(KEY_PARAM);
        }
    }

    @Override
    public React<Boolean> auth(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) {
        return React.emitter(() -> {
            String credential = null;
            if (header) {
                credential = request.getHeader(param);
            } else {
                credential = request.getParameter(param);
            }
            if (credential == null || !credential.equals(KeyAuth.this.credential)) {
                return false;
            }
            return true;
        });
    }
}
