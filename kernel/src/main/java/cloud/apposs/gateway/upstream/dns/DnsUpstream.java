package cloud.apposs.gateway.upstream.dns;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.upstream.AbstractUpstream;
import cloud.apposs.gateway.upstream.UpstreamType;
import cloud.apposs.okhttp.FormEntity;
import cloud.apposs.okhttp.OkHttp;
import cloud.apposs.okhttp.OkMethod;
import cloud.apposs.okhttp.OkRequest;
import cloud.apposs.react.React;
import cloud.apposs.util.Param;
import cloud.apposs.util.Proxy;

import java.util.Map;

/**
 * 基于DNS的上游服务发现
 */
public class DnsUpstream extends AbstractUpstream {
    public static final String KEY_SERVICE = "service";

    private final String service;

    public DnsUpstream(String id, String name, GatewayContext context, Param configuration, String service) {
        super(id, UpstreamType.DNS.name(), name, context, configuration);
        if (!configuration.containsKey(KEY_SERVICE)) {
            throw new IllegalArgumentException("DnsUpstream parameter service must not be null");
        }
        this.service = service;
    }

    @Override
    public String chooseWebSocketUrl(String path) {
        if (service == null) {
            return null;
        }
        // 将http(s)://domain 转为 ws(s)://domain
        String wsUrl = service;
        if (wsUrl.startsWith("https://")) {
            wsUrl = "wss://" + wsUrl.substring(8);
        } else if (wsUrl.startsWith("http://")) {
            wsUrl = "ws://" + wsUrl.substring(7);
        } else if (!wsUrl.startsWith("ws://") && !wsUrl.startsWith("wss://")) {
            wsUrl = "ws://" + wsUrl;
        }
        return wsUrl + path;
    }

    @Override
    public React<?> request(GatewayHttpRequest request, GatewayHttpResponse response, GatewayContext context) throws Exception {
        FormEntity formEntity = FormEntity.builder();
        Map<String, String> parameters = request.getParameters();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            formEntity.add(entry.getKey(), entry.getValue());
        }
        OkRequest proxy = OkRequest.builder()
                .url(request.getUrl())
                .headers(request.getHeaders())
                .proxyMode(Proxy.Type.PROXYPASS)
                .request(OkMethod.valuesOf(request.getMethod()), formEntity)
                .serviceId(name).key(request.getRemoteAddr().toString());
        OkHttp client = context.getHttpClient();
        return client.execute(proxy);
    }
}
