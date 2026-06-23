package cloud.apposs.gateway.upstream.ai;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayException;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.ai.AIProvider;
import cloud.apposs.gateway.ai.AIProviderApi;
import cloud.apposs.gateway.ai.AIProviderApiFactory;
import cloud.apposs.gateway.upstream.AbstractUpstream;
import cloud.apposs.gateway.upstream.UpstreamType;
import cloud.apposs.okhttp.OkHttp;
import cloud.apposs.okhttp.OkRequest;
import cloud.apposs.okhttp.OkResponse;
import cloud.apposs.react.React;
import cloud.apposs.util.HttpStatus;
import cloud.apposs.util.Param;
import cloud.apposs.util.SseEmitter;

import java.util.Map;
import java.util.Objects;

/**
 * AI服务模型上游，负责统一AI服务的请求转发，
 * 其配置依附于路由配置中的upstream字段，upstream_id字段AI服务模型ID，方便后续模型更新时通过路由upstream_id字段进行查找
 * 配置示例如下：
 * <pre>
 * {
 *     "type": "ai",
 *     "provider": "1295313xxxxxxxx",
 *     "parameters": {
 *         "model": "gpt-3.5-turbo",
 *         "keys": [xxx, xxx],
 *         // 可选配置，客户端若有传递，则优先使用客户端传递的参数
 *         "stream": true,
 *         "systemPrompt": "You are a helpful assistant",
 *         "userPrompt": "hello",
 *         "topP": 0.9,
 *         "temperature": 0.7
 *     }
 * }
 * </pre>
 */
public class AIUpstream extends AbstractUpstream {
    public static final String KEY_AI = "ai";
    public static final String KEY_PROVIDER = "provider";
    public static final String KEY_PARAMETERS = "parameters";

    private String providerId;

    private Param parameters;

    public AIUpstream(String id, String name, GatewayContext context, Param configuration) {
        super(id, UpstreamType.AI.name(), name, context, configuration);
        if (!configuration.containsKey(KEY_AI)) {
            throw new IllegalArgumentException("AIUpstream parameter must contain 'ai' key");
        }
        Param param = configuration.getParam(KEY_AI);
        this.providerId = param.getString(KEY_PROVIDER);
        this.parameters = param.getParam(KEY_PARAMETERS);
    }

    @Override
    public React<?> request(GatewayHttpRequest request, GatewayHttpResponse response, GatewayContext context) throws Exception {
        Param param = request.getParam();
        return React.emitter(() -> {
            AIProvider provider = context.getGlobal().getProvider(providerId);
            if (provider == null) {
                throw new GatewayException(HttpStatus.HTTP_STATUS_404, "AIProvider not found for provideId: " + providerId);
            }
            return provider;
        }).request(provider -> {
            String type = provider.getType();
            AIProviderApi application = AIProviderApiFactory.create(type);
            Map<String, String> headers = request.getHeaders();
            if (Objects.nonNull(parameters)) {
                param.putAll(parameters);
            }
            OkRequest proxy = application.build(provider, param, headers);
            OkHttp client = context.getHttpClient();
            return client.execute(proxy);
        }).map((OkResponse result) -> {
            boolean isStream = param.getBoolean(AIProvider.Parameter.STREAM, false);
            if (!isStream) {
                return result.getContent();
            }
            SseEmitter emitter = SseEmitter.builder(result.getContent());
            if (result.isCompleted()) {
                emitter.done(true, false);
            }
            return emitter;
        });
    }
}
