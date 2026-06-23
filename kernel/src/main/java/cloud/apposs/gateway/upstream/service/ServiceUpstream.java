package cloud.apposs.gateway.upstream.service;

import cloud.apposs.balance.IRule;
import cloud.apposs.balance.rule.RuleFactory;
import cloud.apposs.discovery.IDiscovery;
import cloud.apposs.discovery.NacosDiscovery;
import cloud.apposs.discovery.ZooKeeperDiscovery;
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

import java.util.Map;

/**
 * 基于服务发现的上游转发，负责转发请求到对应的服务，服务发现可以基于Zookeeper、Consul、Eureka等
 * 配置示例：
 * <pre>
 * {
 *     "type": "service",
 *     "name": "ServiceUpstream",
 *     "service": {
 *        "type": "zookeeper",
 *        "server": "192.168.1.1:2181",
 *        "path": "/cloud/apposs/gateway",
 *        "name": "node-acct"
 *     }
 * }
 * </pre>
 */
public class ServiceUpstream extends AbstractUpstream {
    public static final String KEY_SERVICE = "service";
    public static final String KEY_SERVER = "server";
    public static final String KEY_PATH = "path";
    public static final String KEY_TYPE = "type";
    public static final String KEY_NAME = "name";

    // 服务发现类型，如zookeeper、consul、eureka等
    private final String serviceType;
    // 服务发现服务器地址
    private final String server;
    // 服务发现路径
    private final String servicePath;
    // 服务发现名称
    private final String serviceName;

    private IDiscovery discovery = null;

    public ServiceUpstream(String id, String name, GatewayContext context, Param configuration) throws Exception {
        super(id, UpstreamType.SERVICE.name(), name, context, configuration);
        if (!configuration.containsKey(KEY_SERVICE)) {
            throw new IllegalArgumentException("ServiceUpstream parameter service must not be null");
        }
        Param serviceConfig = configuration.getParam(KEY_SERVICE);
        this.server = serviceConfig.getString(KEY_SERVER);
        this.servicePath = serviceConfig.getString(KEY_PATH);
        this.serviceType = serviceConfig.getString(KEY_TYPE);
        this.serviceName = serviceConfig.getString(KEY_NAME);
        if ("zookeeper".equalsIgnoreCase(serviceType)) {
            this.discovery = new ZooKeeperDiscovery(serviceName, server, servicePath);
        } else if ("nacos".equalsIgnoreCase(serviceType)) {
            this.discovery = new NacosDiscovery(serviceName, server, servicePath);
        }
        if (this.discovery == null) {
            throw new IllegalArgumentException("ServiceUpstream " + id + " type not support, type=" + serviceType);
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
        ServiceInstance instance = discovery.choose(serviceName);
        if (instance == null) {
            return null;
        }
        return "ws://" + instance.getHost() + ":" + instance.getPort() + path;
    }

    @Override
    public React<?> request(GatewayHttpRequest request, GatewayHttpResponse response, GatewayContext context) throws Exception {
        // 通过服务发现获取服务节点信息
        FormEntity formEntity = FormEntity.builder();
        Map<String, String> parameters = request.getParameters();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            formEntity.add(entry.getKey(), entry.getValue());
        }
        OkRequest proxy = OkRequest.builder()
                .url(request.getUrl())
                .headers(request.getHeaders())
                .proxyMode(Proxy.Type.SERVICE)
                .request(OkMethod.valuesOf(request.getMethod()), formEntity)
                .serviceId(serviceName).key(request.getRemoteAddr().toString());
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
