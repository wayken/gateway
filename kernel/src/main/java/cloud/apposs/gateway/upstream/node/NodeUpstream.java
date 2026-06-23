package cloud.apposs.gateway.upstream.node;

import cloud.apposs.balance.IPing;
import cloud.apposs.balance.IRule;
import cloud.apposs.balance.Peer;
import cloud.apposs.balance.ping.PingFactory;
import cloud.apposs.balance.rule.RuleFactory;
import cloud.apposs.discovery.IDiscovery;
import cloud.apposs.discovery.MemoryDiscovery;
import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.upstream.AbstractUpstream;
import cloud.apposs.gateway.upstream.UpstreamConstant;
import cloud.apposs.gateway.upstream.UpstreamType;
import cloud.apposs.okhttp.*;
import cloud.apposs.react.React;
import cloud.apposs.registry.ServiceInstance;
import cloud.apposs.util.Param;
import cloud.apposs.util.Proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于节点配置的上游转发，负责转发请求到对应的节点
 * 配置示例：
 * <pre>
 *     "type": "node",
 *     "name": "NodeUpstream",
 *     "algorithm":"lessconn",
 *     "healthy": {
 *       "proactive": true,
 *       "passive": true,
 *       "type": "http | https | tcp",
 *       "interval": 5000,
 *       "path": "/health",
 *       "timeout": 2000,
 *       "status": "200, 204, 301, 302, 404"
 *     },
 *     "nodes": [
 *       {
 *         "host": "127.0.0.1",
 *         "port": 8080,
 *         "schema": "http",
 *         "weight": 1
 *       }
 *     ]
 * </pre>
 */
public class NodeUpstream extends AbstractUpstream {
    public static final String KEY_NODES = "nodes";
    public static final String KEY_HOST = "host";
    public static final String KEY_PORT = "port";
    public static final String KEY_WEIGHT = "weight";
    public static final String KEY_SCHEMA = "schema";

    public static final String KEY_HEALTHY = "healthy";
    public static final String KEY_HEALTHY_TYPE = "type";
    public static final String KEY_HEALTHY_PROACTIVE = "proactive";
    public static final String KEY_HEALTHY_PASSIVE = "passive";

    private final IDiscovery discovery;

    private final Map<String, List<Peer>> peers = new HashMap<String, List<Peer>>();

    public NodeUpstream(String id, String name, GatewayContext context, Param configuration) throws Exception {
        super(id, UpstreamType.NODE.name(), name, context, configuration);
        if (!configuration.containsKey(KEY_NODES)) {
            throw new IllegalArgumentException("NodeUpstream parameter nodes must not be null");
        }
        // 创建节点列表
        List<Param> nodes = configuration.getList(KEY_NODES);
        List<Peer> peerList = new ArrayList<>(nodes.size());
        for (Param node : nodes) {
            String host = node.getString(KEY_HOST);
            int port = node.getInt(KEY_PORT);
            String schema = node.getString(KEY_SCHEMA, "http");
            Peer peer = new Peer(host, port);
            peer.addMetadata(Metadata.SCHEMA, node.getString(Metadata.SCHEMA, schema));
            peerList.add(peer);
        }
        this.peers.put(name, peerList);
        // 创建服务发现，设置负载均衡策略和健康检查策略
        Param healthy = configuration.getParam(KEY_HEALTHY, Param.builder());
        boolean isHealthyProactive = healthy.getBoolean(KEY_HEALTHY_PROACTIVE, false);
        this.discovery = new MemoryDiscovery(isHealthyProactive, peers);
        boolean isHealthyPassive = healthy.getBoolean(KEY_HEALTHY_PASSIVE, false);
        if (isHealthyPassive) {
            String healthyType = healthy.getString(KEY_HEALTHY_TYPE, "SocketPing");
            IPing ping = PingFactory.createPing(healthyType);
            this.discovery.setPing(this.name, ping);
        }
        String algorithm = configuration.getString(UpstreamConstant.KEY_ALGORITHM, RuleFactory.RULE_TYPE_ROUNDROBIN);
        IRule rule = RuleFactory.createRule(algorithm);
        if (rule != null) {
            this.discovery.setRule(this.name, rule);
        }
        this.discovery.start();
    }

    @Override
    public String chooseWebSocketUrl(String path) {
        ServiceInstance instance = discovery.choose(name);
        if (instance == null) {
            return null;
        }
        String schema = "ws";
        Param metadata = instance.getMetadata();
        if (metadata != null) {
            String nodeSchema = metadata.getString(Metadata.SCHEMA, "http");
            schema = "https".equals(nodeSchema) ? "wss" : "ws";
        }
        return schema + "://" + instance.getHost() + ":" + instance.getPort() + path;
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
        return client.execute(proxy, discovery).map((OkResponse reply) -> {
            return reply.getBuffer();
        });
    }

    @Override
    public void shutdown() {
        if (discovery != null) {
            discovery.shutdown();
        }
    }
}
