package cloud.apposs.gateway.upstream;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayException;
import cloud.apposs.gateway.upstream.ai.AIUpstream;
import cloud.apposs.gateway.upstream.dns.DnsUpstream;
import cloud.apposs.gateway.upstream.echo.EchoUpstream;
import cloud.apposs.gateway.upstream.index.IndexUpstream;
import cloud.apposs.gateway.upstream.node.NodeUpstream;
import cloud.apposs.gateway.upstream.service.ServiceUpstream;
import cloud.apposs.util.HttpStatus;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;

public final class UpstreamParser {
    private UpstreamParser() {
    }

    /**
     * 解析上游配置，内容格式如下：
     * <pre>
     *   {
     *     "name": "IndexUpstream",
     *     "type": "index"
     *     ...
     *   }
     * </pre>
     *
     * @param  id      上游ID
     * @param  content 上游配置内容
     * @param  context 网关上下文
     * @return 上游对象，如果无法找到匹配的上游则返回null
     * @throws Exception 如果配置不符合规范则抛出异常
     */
    public static Upstream parse(String id, String content, GatewayContext context) throws Exception {
        Param infomation = JsonUtil.parseJsonParam(content);
        return parse(id, infomation, context);
    }

    public static Upstream parse(String id, Param infomation, GatewayContext context) throws Exception {
        if (infomation == null || infomation.isEmpty()) {
            throw new GatewayException(HttpStatus.HTTP_STATUS_500, "Upstream: " + id + " config is invalid");
        }
        // 解析上游基本配置
        String name = infomation.getString(UpstreamConstant.KEY_NAME);
        String type = infomation.getString(UpstreamConstant.KEY_TYPE);
        if (name == null || type == null) {
            throw new GatewayException(HttpStatus.HTTP_STATUS_500, "Upstream: " + id + " config is invalid");
        }
        type = type.toLowerCase();
        // 根据上游类型解析不同的上游对象
        if (UpstreamType.INDEX.matched(type)) {
            return new IndexUpstream(id, name, context, infomation);
        } else if(UpstreamType.ECHO.matched(type)) {
            return new EchoUpstream(id, name, context, infomation);
        } else if(UpstreamType.NODE.matched(type)) {
            return new NodeUpstream(id, name, context, infomation);
        } else if(UpstreamType.DNS.matched(type)) {
            String service = infomation.getString(DnsUpstream.KEY_SERVICE);
            return new DnsUpstream(id, name, context, infomation, service);
        } else if(UpstreamType.SERVICE.matched(type)) {
            return new ServiceUpstream(id, name, context, infomation);
        } else if(UpstreamType.AI.matched(type)) {
            return new AIUpstream(id, name, context, infomation);
        } else if (UpstreamType.CUSTOM.matched(type)) {
            // 如果是业务自定义上游，则通过网关上下文IOC容器来获取
            String upstreamClassName = infomation.getString(UpstreamConstant.KEY_CLASS);
            if (upstreamClassName == null || upstreamClassName.isEmpty()) {
                return null;
            }
            return (Upstream) context.getCompiler().getBean(upstreamClassName);
        }
        return null;
    }
}
