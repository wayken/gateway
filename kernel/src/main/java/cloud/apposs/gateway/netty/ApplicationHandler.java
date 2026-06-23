package cloud.apposs.gateway.netty;

import cloud.apposs.gateway.*;
import cloud.apposs.gateway.plugin.Plugin;
import cloud.apposs.gateway.plugin.PluginResult;
import cloud.apposs.gateway.render.ReactSubcriber;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.route.Router;
import cloud.apposs.gateway.rules.Facts;
import cloud.apposs.gateway.rules.http.HttpFactsBuilder;
import cloud.apposs.gateway.util.WebUtil;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.gateway.zone.Zones;
import cloud.apposs.logger.Logger;
import cloud.apposs.react.IoEmitter;
import cloud.apposs.react.OperateorIntercept;
import cloud.apposs.react.React;
import cloud.apposs.util.HttpStatus;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ApplicationHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private ApplicationContext context;

    private final GatewayConfig config;

    private final Zones zones;

    /**
     * 网关统一异常处理服务
     */
    private final GatewayExceptionResolver resolver = new GatewayExceptionResolver();

    public ApplicationHandler(ApplicationContext context,  GatewayConfig config, Zones zones) {
        this.context = context;
        this.config = config;
        this.zones = zones;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest fullHttpRequest) throws Exception {
        NettyHttpRequest request = NettyUtil.parseRequestParameter(context, fullHttpRequest, config);
        NettyHttpResponse response = new NettyHttpResponse(context);
        try {
            // 根据请求路径获取对应匹配的 Zone 区域
            String host = WebUtil.getRequestHost(request);
            Zone zone = zones.matchZone(host);
            if (zone == null) {
                throw new GatewayException(HttpStatus.HTTP_STATUS_404, "No Mapping Zone Found For HTTP Request With Host [" + host + "]");
            }
            // 构建FACTS，用于后续插件规则判断
            Facts facts = HttpFactsBuilder.build(this.context.getContext(), request);
            request.setAttribute(GatewayConstants.REQUEST_ATTRIBUTE_RULES_FACTS, facts);
            // 根据请求路径获取对应匹配的区域 Route 路由
            Router router = zone.getRouter();
            Route route = router.matchRoute(request, response);
            if (route == null) {
                throw new GatewayException(HttpStatus.HTTP_STATUS_404,
                        "No Mapping Route Found For HTTP Request With URI [" + WebUtil.getRequestPath(request) + "]");
            }
            if (Logger.isDebugEnabled()) {
                Logger.debug("Matched %s for %s of HTTP Request %s", zone, route, request.getUrl());
            }
            // 创建异步拦截器
            Set<Plugin> pluginList = zone.getPluginList();
            List<React<PluginResult>> filterList = new LinkedList<>();
            for (Plugin plugin : pluginList) {
                filterList.add(plugin.preFilter(request, response, zone, route));
            }
            React<?> rxIo = React.intercept(filterList, result -> {
                if (result == null) {
                    return OperateorIntercept.IResult.FAILURE;
                }
                if (!result.success()) {
                    return OperateorIntercept.IResult.FAILURE;
                }
                if (result.skip()) {
                    return OperateorIntercept.IResult.SKIP;
                }
                return OperateorIntercept.IResult.SUCCESS;
            }, (IoEmitter<React<?>>) () -> {
                // 执行异步操作
                return route.route(request, response, this.context.getContext());
            });
            // 异步处理响应结果
            rxIo.subscribe(new ReactSubcriber(zone, route, request, response, resolver)).start();
        } catch (Throwable ex) {
            resolver.resolveException(request, response, ex);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        Logger.error(cause, "Server Transport From %s Internal Error by %s,", context.channel().remoteAddress(), cause.getMessage());
        context.close();
    }
}
