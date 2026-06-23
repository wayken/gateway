package cloud.apposs.gateway.netty;

import cloud.apposs.logger.Logger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;

import java.net.URI;

/**
 * WebSocket反向代理处理器，负责：
 * <pre>
 *     1. 接收客户端WebSocket握手请求并完成服务端握手
 *     2. 建立到上游WebSocket服务的客户端连接
 *     3. 双向转发WebSocket帧，即客户端发送的帧转发到上游服务，上游服务发送的帧转发到客户端
 * </pre>
 */
public class WebSocketProxyHandler extends SimpleChannelInboundHandler<Object> {
    private final String upstreamUrl;
    private Channel upstreamChannel;
    private WebSocketServerHandshaker handshaker;

    public WebSocketProxyHandler(String upstreamUrl) {
        this.upstreamUrl = upstreamUrl;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, Object message) throws Exception {
        if (message instanceof FullHttpRequest) {
            handleHttpRequest(context, (FullHttpRequest) message);
        } else if (message instanceof WebSocketFrame) {
            handleWebSocketFrame(context, (WebSocketFrame) message);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // 先验证握手版本
        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), null, true, 65536);
        handshaker = factory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            return;
        }

        // 先连接上游，成功后再完成客户端握手
        req.retain();
        handleConnectToUpstream(ctx, req);
    }

    private void handleConnectToUpstream(ChannelHandlerContext context, FullHttpRequest request) throws Exception {
        URI uri = new URI(upstreamUrl);
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            port = "wss".equals(scheme) ? 443 : 80;
        }

        WebSocketClientHandshaker clientHandshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());

        Bootstrap bootstrap = new Bootstrap();
        Channel inboundChannel = context.channel();
        bootstrap.group(inboundChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new HttpClientCodec());
                        p.addLast(new HttpObjectAggregator(65536));
                        p.addLast(new WebSocketClientProtocolHandler(clientHandshaker));
                        p.addLast(new BackendHandler(inboundChannel));
                    }
                });

        bootstrap.connect(host, port).addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                upstreamChannel = future.channel();
                // 上游连接成功，完成与客户端的握手
                handshaker.handshake(context.channel(), request);
            } else {
                Logger.error(future.cause(), "Failed to connect to upstream WebSocket %s", upstreamUrl);
                // 上游不可用，返回HTTP 503通知客户端
                if (context.channel().isActive()) {
                    DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1, HttpResponseStatus.SERVICE_UNAVAILABLE);
                    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                    context.channel().writeAndFlush(response)
                            .addListener(ChannelFutureListener.CLOSE);
                }
            }
            request.release();
        });
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            if (upstreamChannel != null && upstreamChannel.isActive()) {
                upstreamChannel.writeAndFlush(frame.retain());
            }
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (upstreamChannel != null && upstreamChannel.isActive()) {
            upstreamChannel.writeAndFlush(frame.retain());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (upstreamChannel != null && upstreamChannel.isActive()) {
            upstreamChannel.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.error(cause, "WebSocket proxy error");
        ctx.close();
    }

    private String getWebSocketLocation(FullHttpRequest req) {
        return "ws://" + req.headers().get("Host") + req.uri();
    }

    /**
     * 上游后端处理器，将上游返回的WebSocket帧转发回客户端
     */
    private static class BackendHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
        private final Channel inboundChannel;

        BackendHandler(Channel inboundChannel) {
            this.inboundChannel = inboundChannel;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
            if (inboundChannel.isActive()) {
                inboundChannel.writeAndFlush(frame.retain());
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            if (inboundChannel.isActive()) {
                inboundChannel.close();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Logger.error(cause, "WebSocket upstream backend error");
            ctx.close();
        }
    }
}
