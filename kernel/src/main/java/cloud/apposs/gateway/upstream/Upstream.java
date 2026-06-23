package cloud.apposs.gateway.upstream;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.react.React;

/**
 * 上游转发接口服务，负责处理HTTP请求转发到上游服务，
 * 对应注册中心中的${/gateway/upstreams/...}配置，子节点为ID下详细配置，全局单例，
 * 可以业务业务自定义上游，负责对HTTP进行协议转换和转发
 */
public interface Upstream {
    /**
     * 获取上游服务ID
     */
    String getId();

    /**
     * 获取上游服务名称
     */
    String getName();

    /**
     * 获取上游服务类型，详见{@link UpstreamType}
     */
    String getType();

    /**
     * 处理HTTP请求，根据处理返回标志
     *
     * @return 异步处理结果
     */
    React<?> request(GatewayHttpRequest request, GatewayHttpResponse response, GatewayContext context) throws Exception;

    /**
     * 关闭上游服务，释放资源，如：IDiscovery、ILoadBalancer等资源
     */
    void shutdown();
}
