package cloud.apposs.gateway.global.auth.server;

import cloud.apposs.balance.IRule;
import cloud.apposs.balance.Peer;
import cloud.apposs.balance.rule.RuleFactory;
import cloud.apposs.discovery.IDiscovery;
import cloud.apposs.discovery.MemoryDiscovery;
import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.global.auth.AbstractAuth;
import cloud.apposs.gateway.global.auth.AuthType;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.upstream.UpstreamConstant;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.okhttp.*;
import cloud.apposs.react.React;
import cloud.apposs.util.Param;
import cloud.apposs.util.Proxy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 基于后端服务进行权限认证，配置示例如下：
 * <pre>
 * {
 *     "type": "server",
 *     "nodes": [
 *         {
 *             "host": "127.0.0.1",
 *             "port": 8080,
 *             "schema": "http"
 *         }
 *         ...
 *     ]
 * }
 * </pre>
 */
public class ServerAuth extends AbstractAuth {
    public static final String KEY_NODES = "nodes";
    public static final String KEY_HOST = "host";
    public static final String KEY_PORT = "port";
    public static final String KEY_SCHEMA = "schema";

    private final IDiscovery discovery;

    private final Map<String, List<Peer>> peers = new HashMap<String, List<Peer>>();

    public ServerAuth(String id, String name, GatewayContext context, Param configuration) throws Exception {
        super(id, AuthType.SERVER.name(), name, context, configuration);
        if (!configuration.containsKey(KEY_NODES)) {
            throw new IllegalArgumentException("ServerAuth parameter nodes must not be null");
        }
        // 创建节点列表
        List<Param> nodes = configuration.getList(KEY_NODES);
        List<Peer> peerList = new LinkedList<Peer>();
        for (Param node : nodes) {
            String host = node.getString(KEY_HOST);
            int port = node.getInt(KEY_PORT);
            String schema = node.getString(KEY_SCHEMA, "http");
            Peer peer = new Peer(host, port);
            peer.addMetadata(Metadata.SCHEMA, node.getString(Metadata.SCHEMA, schema));
            peerList.add(peer);
        }
        this.peers.put(name, peerList);
        // 创建服务发现，设置负载均衡策略
        this.discovery = new MemoryDiscovery(peers);
        String algorithm = configuration.getString(UpstreamConstant.KEY_ALGORITHM, RuleFactory.RULE_TYPE_ROUNDROBIN);
        IRule rule = RuleFactory.createRule(algorithm);
        if (rule != null) {
            this.discovery.setRule(this.name, rule);
        }
        this.discovery.start();
    }

    @Override
    public React<Boolean> auth(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) throws Exception {
        // 将请求转发到后端鉴权服务
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
        return client.execute(proxy, discovery).map((OkResponse reply) -> {
            return reply.getStatus() == 200;
        });
    }
}
