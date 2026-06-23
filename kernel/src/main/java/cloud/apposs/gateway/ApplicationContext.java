package cloud.apposs.gateway;

import cloud.apposs.gateway.common.Banner;
import cloud.apposs.gateway.global.Global;
import cloud.apposs.gateway.hotloader.HotLoader;
import cloud.apposs.gateway.listener.Listener;
import cloud.apposs.gateway.listener.ListenerSupport;
import cloud.apposs.gateway.plugin.Plugin;
import cloud.apposs.gateway.plugin.PluginSupport;
import cloud.apposs.gateway.upstream.Upstream;
import cloud.apposs.gateway.watch.WatchSupport;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.gateway.zone.Zones;
import cloud.apposs.ioc.BeanDefinition;
import cloud.apposs.logger.Configuration;
import cloud.apposs.logger.Logger;
import cloud.apposs.util.StrUtil;
import cloud.apposs.util.SystemInfo;

import java.util.List;
import java.util.Properties;

/**
 * 服务启动接口，由具体的网络内核实现
 */
public abstract class ApplicationContext {
    /** 全局配置 */
    protected GatewayConfig config;

    /** 服务启动开始时间 */
    protected long appStartTime;

    /** 配置服务注册中心 */
    protected WatchSupport watchSupport;

    /** 网关全局上下文，供网关其他模块使用 */
    protected final GatewayContext context;

    /** 插件热加载服务 */
    private HotLoader hotLoader;

    /** Zone区域管理器 */
    protected final Zones zones = new Zones();

    /** 全局配置信息 */
    protected final Global global = new Global();

    public ApplicationContext() throws Exception {
        this(new GatewayConfig());
    }

    public ApplicationContext(GatewayConfig config) throws Exception {
        this.config = config;
        this.context = new GatewayContext(config, global, zones);
    }

    /**
     * 启动HTTP服务
     */
    public ApplicationContext run(String... args) throws Exception {
        appStartTime = System.currentTimeMillis();
        try {
            // 初始化日志
            handleInitSysLogger(config);
            // 输出BANNER信息
            if (config.isBanner()) {
                Banner banner = new Banner();
                banner.printBanner(System.out);
            }
            // 输出系统信息
            handlePrintSysInfomation();

            // 启动插件热加载检测服务
            handleInitHotLoadr(config, context);
            // 初始化注册监听服务和路由表
            handleInitWatchService(context);

            // 开始启动网关服务
            handleStartServer(config, context);
            registerShutdownHook();
            StringBuilder message = new StringBuilder();
            message.append(config.getHttpHost()).append(":").append(config.getHttpPort()).append("[PLAIN]");
            if (config.isHttpsEnable()) {
                message.append(", ").append(config.getHttpsHost()).append(":").append(config.getHttpsPort()).append("[SSL]");
            }
            if (config.isManagementEnable()) {
                message.append(", ").append(config.getManagementHost()).append(":").append(config.getManagementPort()).append("[MANAGEMENT]");
            }
            Logger.info("Gateway Server Startup In %d MilliSeconds, Listen: %s", (System.currentTimeMillis() - appStartTime), message);
        } catch (Exception cause) {
            Logger.error(cause, "Gateway Server Startup Failure, Shutdown Now!");
            shutdown(true);
        }
        return this;
    }

    public GatewayContext getContext() {
        return context;
    }

    /**
     * 添加自定义扩展服务，包括自定义{@link Upstream}和{@link Plugin}扩展，
     * 注意：因为是属于服务自定义内置，每次扩展自定义服务都需要重启网关
     *
     * @return 添加成功返回true，否则返回false
     */
    public boolean addCompilerDefinition(Class<?> clazz) {
        BeanDefinition beanDefinition = new BeanDefinition(clazz, false);
        return context.getCompiler().addBeanDefinition(beanDefinition);
    }

    /**
     * 初始化日志
     */
    private void handleInitSysLogger(GatewayConfig config) {
        Properties properties = new Properties();
        properties.put(Configuration.Prefix.APPENDER, config.getSysLogAppender());
        properties.put(Configuration.Prefix.LEVEL, config.getSysLogLevel());
        properties.put(Configuration.Prefix.FILE, config.getSysLogPath());
        properties.put(Configuration.Prefix.FORMAT, config.getSysLogFormat());
        Logger.config(properties);
    }

    /**
     * 输出系统信息
     */
    private void handlePrintSysInfomation() {
        SystemInfo OS = SystemInfo.getInstance();
        Logger.info("OS Name: %s", OS.getOsName());
        Logger.info("OS Arch: %s", OS.getOsArch());
        Logger.info("IO Mode: %s", config.getIoMode());
        Logger.info("User Linux Epoll: %s", config.isUseLinuxEpoll());
        Logger.info("Proxy Pool Size: %s", config.getProxyPoolSize());
        Logger.info("Configuration Watch Type: %s", config.getWatchType());
        Logger.info("Java Home: %s", OS.getJavaHome());
        Logger.info("Java Version: %s", OS.getJavaVersion());
        Logger.info("Java Vendor: %s", OS.getJavaVendor());
        Logger.info("JVM Max Memory: %s", SystemInfo.getInstance().getMaxMemory() / (1024 * 1024) + " MB");
        Logger.info("JVM Allocated Memory: %s", SystemInfo.getInstance().getTotalMemory() / (1024 * 1024) + " MB");
        List<String> jvmArguments = OS.getJvmArguments();
        for (String argument : jvmArguments) {
            Logger.info("Jvm Argument: [%s]", argument);
        }
    }

    /**
     * 启动插件热加载检测服务
     */
    private void handleInitHotLoadr(GatewayConfig config, GatewayContext context) {
        hotLoader = new HotLoader(config, context);
        hotLoader.scan();
    }

    /**
     * 初始化注册监听服务和路由表
     */
    private void handleInitWatchService(GatewayContext context) throws Exception {
        // 启动注册监听服务
        watchSupport = new WatchSupport(config, context).start();
        Logger.info("Watch Service %s:%s Startup Success", config.getWatchType(), config.getWatchPath());
        // 从注册中心拉取Zone配置列表
        zones.addAllZone(watchSupport.pullZones());
        // 添加区域通用插件
        for (Zone zone : zones.getZones()) {
            for (Plugin plugin : PluginSupport.getCommonPluginList(context)) {
                plugin.registerWatcher(zone, context, watchSupport.getWatch());
                zone.addPlugin(plugin);
            }
            for (Listener listener : ListenerSupport.getCommonListenerList(context)) {
                listener.initialize(zone, context.getConfig());
                zone.addListener(listener);
            }
        }
    }

    /**
     * 注册服务被kill时的回调，只能捕获kill -15的信号量 kill -9 没办法
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    public void shutdown() {
        shutdown(false);
    }

    /**
     * 关闭网关服务
     */
    public void shutdown(boolean force) {
        handleCloseServer();
        if (watchSupport != null) {
            watchSupport.close();
        }
        if (context != null) {
            context.shutdown();
        }
        Logger.info("Gateway Server Has Been Shutdown. Running %s", StrUtil.formatTimeOutput(System.currentTimeMillis() - appStartTime));
        Logger.close(true);
        if (force) {
            System.exit(0);
        }
    }

    /**
     * 启动网关服务，由网络内核服务（如Netty/Undertow）根据自身服务特点启动
     *
     * @param config  网关服务配置
     * @param context 网关服务上下文
     */
    protected abstract void handleStartServer(GatewayConfig config, GatewayContext context) throws Exception;

    /**
     * 关闭服务，释放资源
     */
    protected abstract void handleCloseServer();
}
