package cloud.apposs.gateway;

import cloud.apposs.cache.CacheManager;
import cloud.apposs.gateway.global.Global;
import cloud.apposs.gateway.plugin.runner.auth.AuthPlugin;
import cloud.apposs.gateway.plugin.runner.cache.CachePlugin;
import cloud.apposs.gateway.plugin.runner.ip2region.Ip2RegionPlugin;
import cloud.apposs.gateway.plugin.runner.limit.LimitPlugin;
import cloud.apposs.gateway.plugin.runner.logger.LoggerPlugin;
import cloud.apposs.gateway.plugin.runner.realip.RealIpPlugin;
import cloud.apposs.gateway.plugin.runner.redirect.RedirectPlugin;
import cloud.apposs.gateway.plugin.runner.rewrite.RewritePlugin;
import cloud.apposs.gateway.plugin.runner.waf.WafPlugin;
import cloud.apposs.gateway.zone.Zones;
import cloud.apposs.ioc.BeanDefinition;
import cloud.apposs.ioc.BeanFactory;
import cloud.apposs.okhttp.HttpBuilder;
import cloud.apposs.okhttp.OkHttp;

/**
 * 网关全局上下文，包括IOC容器，供其他内部模块共同引用
 */
public final class GatewayContext {
    private final GatewayConfig configuration;

    /** IOC容器，负责动态加载网关插件 */
    private final BeanFactory compiler;

    /** Zone区域管理器 */
    private final Zones zones;

    /** 网关全局配置 */
    private final Global global;

    /** HTTP代理转发客户端，主要负责网关内部服务节点代理转发 */
    private final OkHttp httpClient;

    /** 网关自带通用插件列表 */
    private static Class<?>[] gatewayPluginClasses = new Class<?>[] {
            LoggerPlugin.class,
            RealIpPlugin.class,
            Ip2RegionPlugin.class,
            AuthPlugin.class,
            RedirectPlugin.class,
            WafPlugin.class,
            LimitPlugin.class,
            CachePlugin.class,
            RewritePlugin.class
    };

    public GatewayContext(GatewayConfig configuration, Global global, Zones zones) throws Exception {
        this.configuration = configuration;
        this.global = global;
        this.zones = zones;
        // 创建IOC容器，进去后续的网关插件动态加载
        this.compiler = new BeanFactory();
        compiler.addBean(configuration);
        compiler.addBean(global);
        // 初始化网关缓存
        CacheManager cacheManager = new CacheManager(configuration.getCacheConfig());
        compiler.addBean(cacheManager);
        // 初始化网关自带插件元数据到IOC容器
        for (Class<?> pluginClass : gatewayPluginClasses) {
            BeanDefinition definition = new BeanDefinition(pluginClass, false);
            compiler.addBeanDefinition(definition);
        }
        // 初始化HTTP客户端
        String ioMode = HttpBuilder.IO_MODE_NETTY;
        this.httpClient = HttpBuilder.builder()
                .poolConnections(configuration.getProxyPoolSize())
                .connectTimeout(configuration.getProxyConnectTimeout())
                .socketTimeout(configuration.getProxySocketTimeout()).loopSize(1).ioMode(ioMode).build();
    }

    /**
     * 获取网关配置
     */
    public GatewayConfig getConfig() {
        return configuration;
    }

    /**
     * 获取所有区域列表
     */
    public Zones getZones() {
        return zones;
    }

    /**
     * 获取全局配置
     */
    public Global getGlobal() {
        return global;
    }

    /**
     * 获取IOC容器
     */
    public BeanFactory getCompiler() {
        return compiler;
    }

    /**
     * 获取HTTP代理转发客户端
     */
    public OkHttp getHttpClient() {
        return httpClient;
    }

    public void shutdown() {
        compiler.destroy();
        httpClient.close();
    }
}
