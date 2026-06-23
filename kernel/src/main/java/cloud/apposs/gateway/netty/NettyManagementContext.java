package cloud.apposs.gateway.netty;

import cloud.apposs.gateway.GatewayConfig;
import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.management.ManagementRouter;
import cloud.apposs.logger.Logger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 网关管理服务，负责处理来自管理端的请求，包括健康检查、Prometheus监控、网关静态文件上传等
 */
public class NettyManagementContext  {
    private final GatewayContext context;

    private final GatewayConfig config;

    private final ManagementRouter router;

    public NettyManagementContext(GatewayContext context, GatewayConfig config) throws Exception {
        this.context = context;
        this.config = config;
        this.router = new ManagementRouter(context, config);
    }

    public ApplicationHandler newApplicationHandler() {
        return new ApplicationHandler();
    }

    public class ApplicationHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext context, FullHttpRequest fullHttpRequest) throws Exception {
            NettyHttpRequest request = NettyUtil.parseRequestParameter(context, fullHttpRequest, config);
            NettyHttpResponse response = new NettyHttpResponse(context);
            router.route(request, response, NettyManagementContext.this.context);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
            Logger.error(cause, "Server Management Transport From %s Internal Error by %s,", context.channel().remoteAddress(), cause.getMessage());
            context.close();
        }
    }
}
