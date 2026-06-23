package cloud.apposs.gateway;

import cloud.apposs.cache.CacheConfig;
import cloud.apposs.gateway.config.Value;
import cloud.apposs.logger.Appender;
import cloud.apposs.logger.Logger;
import cloud.apposs.util.StrUtil;

public class GatewayConfig {
    public static final String IO_MODE_NETTY = "netty";
    /** 数据传输时（文件上传）临时文件存储文件 */
    public static final String DEFAULT_TMP_DIRECTORY = System.getProperty("java.io.tmpdir");
    /** 框架当前目录 */
    public static final String DEFAULT_SOURCE_DIR = System.getProperty("user.dir");

    public static final String DEFAULT_ACCESS_LOG_FORMAT =
            "$remote_addr:$remote_port $http_user_agent";

    // 基于 Zookeeper 的注册中心服务
    public static final String WATCH_TYPE_ZOOKEEPER = "zookeeper";
    // 基于文件系统（NFS/EFS等）的注册中心服务
    public static final String WATCH_TYPE_FS = "filesystem";

    /** 绑定服务器地址，端口 */
    @Value("gateway.framework.http.host")
    private String httpHost = GatewayConstants.DEFAULT_HTTP_HOST;
    @Value("gateway.framework.http.port")
    private int httpPort = GatewayConstants.DEFAULT_HTTP_PORT;
    @Value("gateway.framework.https.enable")
    private boolean httpsEnable = false;
    @Value("gateway.framework.https.host")
    private String httpsHost = GatewayConstants.DEFAULT_HTTPS_HOST;
    @Value("gateway.framework.https.port")
    private int httpsPort = GatewayConstants.DEFAULT_HTTPS_PORT;

    /** 网关管理服务相关配置 */
    @Value("gateway.framework.management.enable")
    private boolean managementEnable = false;
    @Value("gateway.framework.management.host")
    private String managementHost = GatewayConstants.DEFAULT_MANAGEMENT_HOST;
    @Value("gateway.framework.management.port")
    private int managementPort = GatewayConstants.DEFAULT_MANAGEMENT_PORT;
    @Value("gateway.framework.management.context_path")
    private String managementContextPath = GatewayConstants.DEFAULT_MANAGEMENT_CONTEXT_PATH;

    /**
     * 底层网格模型，默认是采用NETTY
     */
    @Value("gateway.network.io_mode")
    protected String ioMode = IO_MODE_NETTY;

    /**
     * 插件 LIB 包路径，用于网关扫描指定包下所有插件【可选配置】
     */
    @Value("gateway.plugin.libary")
    protected String pluginLibary = "";

    /** 半连接队列数 */
    @Value("gateway.network.backlog")
    private int backlog = 1024;

    /**
     * SO_REUSEADDR对应于套接字选项中的SO_REUSEADDR，这个参数表示允许重复使用本地地址和端口，
     * 比如，某个服务器进程占用了TCP的80端口进行监听，此时再次监听该端口就会返回错误，使用该参数就可以解决问题，
     * 该参数允许共用该端口，这个在服务器程序中比较常使用，
     * 比如某个进程非正常退出，该程序占用的端口可能要被占用一段时间才能允许其他进程使用，
     * 而且程序死掉以后，内核一需要一定的时间才能够释放此端口，不设置SO_REUSEADDR就无法正常使用该端口
     */
    @Value("gateway.network.reuse-address")
    private boolean reuseAddress = true;

    /**
     * 开启此参数，那么客户端在每次发送数据时，无论数据包的大小都会将这些数据发送出去
     * 参考：
     * http://blog.csdn.net/huang_xw/article/details/7340241
     * http://www.open-open.com/lib/view/open1412994697952.html
     */
    @Value("gateway.network.tcp_nodelay")
    private boolean tcpNoDelay = true;

    /**
     * 多少个EventLoop轮询器，主要用于处理各自网络读写数据，
     * 当服务性能不足可提高此配置提升对网络IO的并发处理，但注意EventLoop业务层必须要做到异步，不能有同步阻塞请求
     */
    @Value("gateway.network.eventloop_count")
    private int numOfGroup = Runtime.getRuntime().availableProcessors() + 1;

    /**
     * 工作线程池数量
     */
    @Value("gateway.network.worker_count")
    private int workerCount = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * 是否采用Linux底层Epoll网络模型，针对底层为NETTY
     * Netty底层会通过Native方法为调用底层Epoll函数，可以提升性能，减少GC
     */
    @Value("gateway.network.use_linux_epoll")
    protected boolean useLinuxEpoll = false;

    @Value("gateway.framework.charset")
    private String charset = GatewayConstants.DEFAULT_CHARSET;

    @Value("gateway.framework.banner")
    private boolean banner = true;

    /**
     * 请求转发连接池大小，用于连接复用，减少连接创建开销，超过此连接池大小则作为短链接请求，不再回收到连接池
     * 如果连接池大小为0则不会进行连接复用，每次请求都会创建新的连接
     */
    @Value("gateway.framework.proxy_pool.pool_size")
    private int proxyPoolSize = 128;

    /**
     * 请求转发连接超时时间，单位毫秒，默认为12秒
     */
    @Value("gateway.framework.proxy_pool.connect_timeout")
    private int proxyConnectTimeout = 12 * 1000;

    /**
     * 请求转发读写超时时间，单位毫秒，默认为60秒
     */
    @Value("gateway.framework.proxy_pool.socket_timeout")
    private int proxySocketTimeout = 60 * 1000;

    /**
     * 网关ID，主要应用于集群，集群内每个网关ID必须唯一
     */
    @Value("gateway.framework.id")
    private String id;

    /**
     * 数据临时目录，用于存储临时数据，如：上传文件，缓存目录，为空则用系统默认临时目录
     */
    @Value("gateway.framework.data_temp_path")
    private String dataTempPath = DEFAULT_TMP_DIRECTORY;

    /**
     * 数据存储目录，用于存储框架数据，如：前端静态文件，缓存目录，为空则用当前项目所在目录
     */
    @Value("gateway.framework.data_source_path")
    private String dataSourcePath = DEFAULT_SOURCE_DIR;

    /**
     * 监听配置中心类型，zookeeper/nacos等
     */
    @Value("gateway.watch.type")
    private String watchType = WATCH_TYPE_FS;

    /**
     * 监听配置中心地址，多个地址用逗号分隔
     */
    @Value("gateway.watch.servers")
    private String watchServers = "";

    /**
     * 监听配置中心地址
     */
    @Value("gateway.watch.path")
    private String watchPath = "/gateway";

    /**
     * 网关读写超时时间
     */
    private int readTimeout = 2 * 60;
    private int sendTimeout = 5 * 60;

    /** 缓存相关配置，用于各网关插件共同使用 */
    @Value("gateway.watch.cache")
    private CacheConfig cacheConfig = new CacheConfig();

    // 系统日志相关配置
    /** 系统日志输出终端 */
    @Value("gateway.syslog.appender")
    private String sysLogAppender = Appender.CONSOLE;
    /**
     * 系统日志输出级别，
     * FATAL（致命）、
     * ERROR（错误）、
     * WARN（警告）、
     * INFO（信息）、
     * DEBUG（调试）、
     * OFF（关闭），
     * 默认为INFO
     */
    @Value("gateway.syslog.level")
    private String sysLogLevel = "INFO";
    /** 系统日志的存储路径 */
    @Value("gateway.syslog.path")
    private String sysLogPath = "logs";
    /** 系统日志输出模板 */
    @Value("gateway.syslog.format")
    private String sysLogFormat = Logger.DEFAULT_LOG_FORMAT;

    // 访问日志相关配置
    /** 访问日志输出终端 */
    @Value("gateway.accesslog.appender")
    private String accessLogAppender = Appender.CONSOLE;
    /** 是否开启访问日志 */
    @Value("gateway.accesslog.enable")
    private boolean accessLogEnable = true;
    /** 访问日志的存储路径 */
    @Value("gateway.accesslog.path")
    private String accessLogPath = "logs";
    /** 访问日志输出模板 */
    @Value("gateway.accesslog.format")
    private String accessLogFormat = DEFAULT_ACCESS_LOG_FORMAT;

    public String getHttpHost() {
        return httpHost;
    }

    public void setHttpHost(String httpHost) {
        this.httpHost = httpHost;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public boolean isHttpsEnable() {
        return httpsEnable;
    }

    public void setHttpsEnable(boolean httpsEnable) {
        this.httpsEnable = httpsEnable;
    }

    public String getHttpsHost() {
        return httpsHost;
    }

    public void setHttpsHost(String httpsHost) {
        this.httpsHost = httpsHost;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }

    public boolean isManagementEnable() {
        return managementEnable;
    }

    public void setManagementEnable(boolean managementEnable) {
        this.managementEnable = managementEnable;
    }

    public String getManagementHost() {
        return managementHost;
    }

    public void setManagementHost(String managementHost) {
        this.managementHost = managementHost;
    }

    public int getManagementPort() {
        return managementPort;
    }

    public void setManagementPort(int managementPort) {
        this.managementPort = managementPort;
    }

    public String getManagementContextPath() {
        return managementContextPath;
    }

    public void setManagementContextPath(String managementContextPath) {
        this.managementContextPath = managementContextPath;
    }

    public String getIoMode() {
        return ioMode;
    }

    public void setIoMode(String ioMode) {
        this.ioMode = ioMode;
    }

    public String getPluginLibary() {
        return pluginLibary;
    }

    public void setPluginLibary(String pluginLibary) {
        this.pluginLibary = pluginLibary;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public boolean isReuseAddress() {
        return reuseAddress;
    }

    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public int getNumOfGroup() {
        return numOfGroup;
    }

    public void setNumOfGroup(int numOfGroup) {
        this.numOfGroup = numOfGroup;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }

    public boolean isUseLinuxEpoll() {
        return useLinuxEpoll;
    }

    public void setUseLinuxEpoll(boolean useLinuxEpoll) {
        this.useLinuxEpoll = useLinuxEpoll;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean isBanner() {
        return banner;
    }

    public void setBanner(boolean banner) {
        this.banner = banner;
    }

    public int getProxyPoolSize() {
        return proxyPoolSize;
    }

    public void setProxyPoolSize(int proxyPoolSize) {
        this.proxyPoolSize = proxyPoolSize;
    }

    public int getProxyConnectTimeout() {
        return proxyConnectTimeout;
    }

    public void setProxyConnectTimeout(int proxyConnectTimeout) {
        this.proxyConnectTimeout = proxyConnectTimeout;
    }

    public int getProxySocketTimeout() {
        return proxySocketTimeout;
    }

    public void setProxySocketTimeout(int proxySocketTimeout) {
        this.proxySocketTimeout = proxySocketTimeout;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataTempPath() {
        return dataTempPath;
    }

    public void setDataTempPath(String dataTempPath) {
        if (StrUtil.isEmpty(dataTempPath)) {
            return;
        }
        this.dataTempPath = dataTempPath;
    }

    public String getDataSourcePath() {
        return dataSourcePath;
    }

    public void setDataSourcePath(String dataSourcePath) {
        if (StrUtil.isEmpty(dataSourcePath)) {
            return;
        }
        this.dataSourcePath = dataSourcePath;
    }

    public String getWatchType() {
        return watchType;
    }

    public void setWatchType(String watchType) {
        this.watchType = watchType;
    }

    public String getWatchServers() {
        return watchServers;
    }

    public void setWatchServers(String watchServers) {
        this.watchServers = watchServers;
    }

    public String getWatchPath() {
        return watchPath;
    }

    public void setWatchPath(String watchPath) {
        this.watchPath = watchPath;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

    public void setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
    }

    public String getSysLogAppender() {
        return sysLogAppender;
    }

    public void setSysLogAppender(String sysLogAppender) {
        this.sysLogAppender = sysLogAppender;
    }

    public String getSysLogLevel() {
        return sysLogLevel;
    }

    public void setSysLogLevel(String sysLogLevel) {
        this.sysLogLevel = sysLogLevel;
    }

    public String getSysLogPath() {
        return sysLogPath;
    }

    public void setSysLogPath(String sysLogPath) {
        this.sysLogPath = sysLogPath;
    }

    public String getSysLogFormat() {
        return sysLogFormat;
    }

    public void setSysLogFormat(String sysLogFormat) {
        this.sysLogFormat = sysLogFormat;
    }

    public String getAccessLogAppender() {
        return accessLogAppender;
    }

    public void setAccessLogAppender(String accessLogAppender) {
        this.accessLogAppender = accessLogAppender;
    }

    public boolean isAccessLogEnable() {
        return accessLogEnable;
    }

    public void setAccessLogEnable(boolean accessLogEnable) {
        this.accessLogEnable = accessLogEnable;
    }

    public String getAccessLogPath() {
        return accessLogPath;
    }

    public void setAccessLogPath(String accessLogPath) {
        this.accessLogPath = accessLogPath;
    }

    public String getAccessLogFormat() {
        return accessLogFormat;
    }

    public void setAccessLogFormat(String accessLogFormat) {
        this.accessLogFormat = accessLogFormat;
    }
}
