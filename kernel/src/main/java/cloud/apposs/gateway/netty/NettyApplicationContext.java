package cloud.apposs.gateway.netty;

import cloud.apposs.gateway.ApplicationContext;
import cloud.apposs.gateway.GatewayConfig;
import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.global.Global;
import cloud.apposs.gateway.global.IGlobalListener;
import cloud.apposs.gateway.global.certificate.Certificate;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SniHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.FutureListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class NettyApplicationContext extends ApplicationContext {
    public static final String HTTP_SNI_HANDLER = "sniHandler";
    public static final String HTTP_REQUEST_DECODER = "httpDecoder";
    public static final String HTTP_ENCODER = "httpEncoder";
    public static final String HTTP_AGGREGATOR = "httpAggregator";
    public static final String HTTP_CHUNKED = "httpChunked";
    public static final String HTTP_GATEWAY = "httpGateway";

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    // 网关管理服务，需要在配置中开启management配置
    private NettyManagementContext managementContext;

    // 域名证书映射服务
    private final DynamicSslContextMapping dynamicSslContextMapping = new DynamicSslContextMapping();

    public NettyApplicationContext() throws Exception {
        super(new GatewayConfig());
    }

    public NettyApplicationContext(GatewayConfig config) throws Exception {
        super(config);
    }

    @Override
    protected void handleStartServer(GatewayConfig config, GatewayContext context) throws Exception {
        Class<? extends ServerChannel> channelClass = null;
        if (config.isUseLinuxEpoll()) {
            this.bossGroup = new EpollEventLoopGroup(config.getNumOfGroup());
            this.workerGroup = new EpollEventLoopGroup(config.getWorkerCount());
            channelClass = EpollServerSocketChannel.class;
        } else {
            this.bossGroup = new NioEventLoopGroup(config.getNumOfGroup());
            this.workerGroup = new NioEventLoopGroup(config.getWorkerCount());
            channelClass = NioServerSocketChannel.class;
        }
        handleHttpServerInitialize(channelClass);
        if (config.isHttpsEnable()) {
            handleHttpsServerInitialize(context, channelClass);
        }
        if (config.isManagementEnable()) {
            handleManagementServerInitialize(channelClass);
        }
    }

    @Override
    protected void handleCloseServer() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    // 启动网关HTTP服务
    private void handleHttpServerInitialize(Class<? extends ServerChannel> channelClass) throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        handleBootstrapInitialize(bootstrap, channelClass, false);
        InetSocketAddress address = new InetSocketAddress(config.getHttpHost(), config.getHttpPort());
        bootstrap.bind(address).addListener((FutureListener<Void>) future -> {
            if (!future.isSuccess()) {
                throw new IOException(future.cause());
            }
        }).sync();
    }

    // 启动网关HTTPS服务
    private void handleHttpsServerInitialize(GatewayContext context, Class<? extends ServerChannel> channelClass) throws Exception {
        // 读取证书并进行SSL初始化，注意如果证书配置为空则起空的SSL服务监听，当配置中心证书更新时再动态更新网关SSL
        List<Certificate> certificateList = context.getGlobal().getCertificates();
        for (Certificate certificate : certificateList) {
            SslContext sslContext = buildSSLContext(certificate);
            dynamicSslContextMapping.put(certificate.getDomain(), sslContext);
        }
        context.getGlobal().addListener(Global.KEY_CERTIFICATES, new HttpsCertificateListener(certificateList));
        ServerBootstrap bootstrap = new ServerBootstrap();
        handleBootstrapInitialize(bootstrap, channelClass, true);
        InetSocketAddress address = new InetSocketAddress(config.getHttpsHost(), config.getHttpsPort());
        bootstrap.bind(address).addListener((FutureListener<Void>) future -> {
            if (!future.isSuccess()) {
                throw new IOException(future.cause());
            }
        }).sync();
    }

    // 启动管理服务
    private void handleManagementServerInitialize(Class<? extends ServerChannel> channelClass) throws Exception {
        this.managementContext = new NettyManagementContext(this.context, config);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(channelClass)
                .option(ChannelOption.SO_BACKLOG, config.getBacklog())
                .option(ChannelOption.SO_REUSEADDR, config.isReuseAddress())
                .childOption(ChannelOption.TCP_NODELAY, config.isTcpNoDelay())
                .childOption(ChannelOption.SO_KEEPALIVE,false)
                .childOption(ChannelOption.SO_REUSEADDR,true)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(HTTP_REQUEST_DECODER, new HttpRequestDecoder());
                        pipeline.addLast(HTTP_ENCODER, new HttpResponseEncoder());
                        pipeline.addLast(HTTP_AGGREGATOR, new HttpObjectAggregator(65535));
                        pipeline.addLast(HTTP_CHUNKED, new ChunkedWriteHandler());
                        pipeline.addLast(HTTP_GATEWAY, managementContext.newApplicationHandler());
                    }
                });
        InetSocketAddress address = new InetSocketAddress(config.getManagementHost(), config.getManagementPort());
        bootstrap.bind(address).addListener((FutureListener<Void>) future -> {
            if (!future.isSuccess()) {
                throw new IOException(future.cause());
            }
        }).sync();
    }

    private ServerBootstrap handleBootstrapInitialize(ServerBootstrap bootstrap, Class<? extends ServerChannel> channelClass, boolean useSSL) {
        bootstrap.group(bossGroup, workerGroup)
                .channel(channelClass)
                .option(ChannelOption.SO_BACKLOG, config.getBacklog())
                .option(ChannelOption.SO_REUSEADDR, config.isReuseAddress())
                .childOption(ChannelOption.TCP_NODELAY, config.isTcpNoDelay())
                .childOption(ChannelOption.SO_KEEPALIVE,false)
                .childOption(ChannelOption.SO_REUSEADDR,true)
                .childHandler(new ChannelHandlerInitializer(useSSL));
        return bootstrap;
    }

    private class ChannelHandlerInitializer extends ChannelInitializer<Channel> {
        private final boolean useSSL;

        public ChannelHandlerInitializer(boolean useSSL) {
            this.useSSL = useSSL;
        }

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            // 如果配置了SSL，则添加SSL处理器
            if (useSSL) {
                SniHandler sniHandler = new SniHandler(dynamicSslContextMapping);
                pipeline.addLast(HTTP_SNI_HANDLER, sniHandler);
            }
            // Server端接收到的是HttpRequest，所以要使用HttpRequestDecoder进行解码
            pipeline.addLast(HTTP_REQUEST_DECODER, new HttpRequestDecoder());
            // Server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
            pipeline.addLast(HTTP_ENCODER, new HttpResponseEncoder());
            // 将多个消息转换为单一的BootorHttpRequest或FullHttpResponse对象
            pipeline.addLast(HTTP_AGGREGATOR, new HttpObjectAggregator(65535));
            // 解决大数据包传输问题，用于支持异步写大量数据流并且不需要消耗大量内存也不会导致内存溢出错误( OutOfMemoryError )。
            // 仅支持ChunkedInput类型的消息。也就是说，仅当消息类型是ChunkedInput时才能实现ChunkedWriteHandler提供的大数据包传输功能
            pipeline.addLast(HTTP_CHUNKED, new ChunkedWriteHandler());
            pipeline.addLast(HTTP_GATEWAY, new ApplicationHandler(NettyApplicationContext.this, config, zones));
        }
    }

    private SslContext buildSSLContext(Certificate certificate) throws Exception {
        // keyData是私钥内容，certData是公钥内容，创建SSLContext
        String keyData = certificate.getKeyData();
        String certData = certificate.getCertData();
        try (
                InputStream certInputStream = new ByteArrayInputStream(certData.getBytes(StandardCharsets.UTF_8));
                InputStream keyInputStream = new ByteArrayInputStream(keyData.getBytes(StandardCharsets.UTF_8))
        ) {
            return SslContextBuilder.forServer(certInputStream, keyInputStream).build();
        }
    }

    private class HttpsCertificateListener implements IGlobalListener {
        private List<Certificate> certificates;

        public HttpsCertificateListener(List<Certificate> certificates) {
            this.certificates = certificates;
        }

        @Override
        public void onGlobalChanged(Global global) throws Exception {
            List<Certificate> globalCertificates = global.getCertificates();
            // 对比certificates和glogalCertificates，情况如下
            // 1. certificates有的域名，glogalCertificates没有，则从DynamicSslContextMapping删除
            // 2. certificates没有的域名，glogalCertificates有的，则添加到DynamicSslContextMapping
            // 3. certificates和glogalCertificates的域名都存在的，则更新DynamicSslContextMapping对应的证书
            for (Certificate certificate : certificates) {
                if (!globalCertificates.contains(certificate)) {
                    dynamicSslContextMapping.remove(certificate.getDomain());
                } else {
                    SslContext sslContext = buildSSLContext(certificate);
                    dynamicSslContextMapping.put(certificate.getDomain(), sslContext);
                }
            }
        }
    }
}
