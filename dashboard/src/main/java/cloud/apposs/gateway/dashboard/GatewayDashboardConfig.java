package cloud.apposs.gateway.dashboard;

import cloud.apposs.bootor.BootorConfig;
import cloud.apposs.gateway.config.Value;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.logger.Appender;
import cloud.apposs.logger.Logger;

@Component
public class GatewayDashboardConfig extends BootorConfig {
    private String basePackage = "cloud.apposs.gateway.dashboard";

    // 基于 Zookeeper 的注册中心服务
    public static final String NODE_TYPE_ZOOKEEPER = "zookeeper";
    // 基于文件系统（NFS/EFS等）的注册中心服务
    public static final String NODE_TYPE_FS = "filesystem";

    /** 绑定服务器地址 */
    @Value("gatewaydashboard.http.host")
    private String host = "0.0.0.0";

    /** 绑定服务器端口 */
    @Value("gatewaydashboard.http.port")
    private int port = 8894;

    @Value("gatewaydashboard.network.io_mode")
    private String ioMode = IO_MODE_NETTY;

    /**
     * 配置节点类型，zookeeper/nacos等
     */
    @Value("gatewaydashboard.node.type")
    private String nodeType = NODE_TYPE_FS;

    /** 节点相关配置 */
    @Value("gatewaydashboard.node.host")
    private String nodeHost = "";
    @Value("gatewaydashboard.node.port")
    private int nodePort = -1;
    @Value("gatewaydashboard.node.path")
    private String nodePath = "/gateway";

    /** 节点相关配置 */
    @Value("gatewaydashboard.api.readonly")
    private boolean readonly = false;

    /** 数据库相关配置 */
    @Value("gatewaydashboard.database.dialect")
    private String databaseDialect = "sqlite";
    @Value("gatewaydashboard.database.url")
    private String databaseUrl = "";
    @Value("gatewaydashboard.database.username")
    private String databaseUsername = "";
    @Value("gatewaydashboard.database.password")
    private String databasePassword = "";
    // 数据库连接池配置
    @Value("gatewaydashboard.database.max_pool_size")
    private int databaseMaxPoolSize = 10;
    @Value("gatewaydashboard.database.min_pool_size")
    private int databaseMinPoolSize = 1;

    /** 缓存相关配置 */
    @Value("gatewaydashboard.cache.type")
    private String cacheType = "jvm";
    @Value("gatewaydashboard.cache.expiration_time")
    private int cacheExpirationTime = 1 * 60 * 60 * 1000;
    @Value("gatewaydashboard.cache.expiration_time_random")
    private boolean cacheExpirationTimeRandom = true;

    /** 日志配置相关 */
    @Value("gatewaydashboard.log.appender")
    protected String logAppender = Appender.CONSOLE;
    @Value("gatewaydashboard.log.level")
    protected String logLevel = "INFO";
    @Value("gatewaydashboard.log.path")
    protected String logPath = "log";
    @Value("gatewaydashboard.log.format")
    protected String logFormat = Logger.DEFAULT_LOG_FORMAT;

    @Override
    public String getBasePackage() {
        return basePackage;
    }

    @Override
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String getIoMode() {
        return ioMode;
    }

    @Override
    public void setIoMode(String ioMode) {
        this.ioMode = ioMode;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeHost() {
        return nodeHost;
    }

    public void setNodeHost(String nodeHost) {
        this.nodeHost = nodeHost;
    }

    public int getNodePort() {
        return nodePort;
    }

    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
    }

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public String getDatabaseDialect() {
        return databaseDialect;
    }

    public void setDatabaseDialect(String databaseDialect) {
        this.databaseDialect = databaseDialect;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public void setDatabaseUsername(String databaseUsername) {
        this.databaseUsername = databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public int getDatabaseMaxPoolSize() {
        return databaseMaxPoolSize;
    }

    public void setDatabaseMaxPoolSize(int databaseMaxPoolSize) {
        this.databaseMaxPoolSize = databaseMaxPoolSize;
    }

    public int getDatabaseMinPoolSize() {
        return databaseMinPoolSize;
    }

    public void setDatabaseMinPoolSize(int databaseMinPoolSize) {
        this.databaseMinPoolSize = databaseMinPoolSize;
    }

    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    public int getCacheExpirationTime() {
        return cacheExpirationTime;
    }

    public void setCacheExpirationTime(int cacheExpirationTime) {
        this.cacheExpirationTime = cacheExpirationTime;
    }

    public boolean isCacheExpirationTimeRandom() {
        return cacheExpirationTimeRandom;
    }

    public void setCacheExpirationTimeRandom(boolean cacheExpirationTimeRandom) {
        this.cacheExpirationTimeRandom = cacheExpirationTimeRandom;
    }

    @Override
    public String getLogAppender() {
        return logAppender;
    }

    @Override
    public void setLogAppender(String logAppender) {
        this.logAppender = logAppender;
    }

    @Override
    public String getLogLevel() {
        return logLevel;
    }

    @Override
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public String getLogPath() {
        return logPath;
    }

    @Override
    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    @Override
    public String getLogFormat() {
        return logFormat;
    }

    @Override
    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }
}
