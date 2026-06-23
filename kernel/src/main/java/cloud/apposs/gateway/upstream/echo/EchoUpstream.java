package cloud.apposs.gateway.upstream.echo;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.upstream.AbstractUpstream;
import cloud.apposs.gateway.upstream.UpstreamType;
import cloud.apposs.react.React;
import cloud.apposs.util.MediaType;
import cloud.apposs.util.Param;

/**
 * 上游直接返回配置内容
 */
public class EchoUpstream extends AbstractUpstream {
    public static final String KEY_CONTENT_TYPE = "contentType";
    public static final String KEY_CODE = "code";
    public static final String KEY_CONTENT = "content";

    /**
     * HTTP响应状态
     */
    private final int status;

    /**
     * HTTP响应类型
     */
    private final String contentType;

    /**
     * 输出HTML内容
     */
    private final String content;

    public EchoUpstream(String id, String name, GatewayContext context, Param configuration) {
        super(id, UpstreamType.ECHO.name(), name, context, configuration);
        if (!configuration.containsKey(KEY_CONTENT)) {
            throw new IllegalArgumentException("EchoUpstream parameter content must not be null");
        }
        this.contentType = configuration.getString(KEY_CONTENT_TYPE, MediaType.TEXT_HTML.toString());
        this.status = configuration.getInt(KEY_CODE, 200);
        this.content = configuration.getString(KEY_CONTENT);
    }

    @Override
    public React<?> request(GatewayHttpRequest request, GatewayHttpResponse response, GatewayContext context) throws Exception {
        return React.emitter(() -> {
            response.setStatus(status);
            response.setContentType(contentType + "; charset=UTF-8");
            return content;
        });
    }
}
