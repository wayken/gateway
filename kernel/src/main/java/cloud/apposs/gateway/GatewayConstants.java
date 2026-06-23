package cloud.apposs.gateway;

public class GatewayConstants {
    public static final String DEFAULT_HTTP_HOST = "0.0.0.0";
    public static final String DEFAULT_HTTPS_HOST = "0.0.0.0";
    public static final String DEFAULT_MANAGEMENT_HOST = "127.0.0.1";
    public static final int DEFAULT_HTTP_PORT = 8892;
    public static final int DEFAULT_HTTPS_PORT = 8894;
    public static final int DEFAULT_MANAGEMENT_PORT = 8900;
    public static final String DEFAULT_CHARSET = "utf-8";
    public static final String DEFAULT_CONTENT_TYPE = "text/html";

    /** 默认配置文件名称 */
    public static final String DEFAULT_CONFIG_FILE = "gateway.yaml";
    public static final String GATEWAY_VERSION = "v1.0.0.RELEASE";

    public static final String GATEWAY_WATCH_PATH = "/gateway";
    public static final String ZONES_PATH_SUFFIX = "/";
    // 网关所有Zone节点，用于管理网关的所有Zone信息，即${gateway.watch.path}/zones
    public static final String GATEWAY_NODE_ZONE = "/zones";
    public static final String GATEWAY_NODE_GLOBAL = "/global";
    public static final String GATEWAY_SEETING_PATH_ZONE = "zone";
    public static final String GATEWAY_SEETING_PATH_PLUGINS = "plugins";
    // 网关所有Service节点，用于网关启动、停止时的服务注册，即${gateway.watch.path}/services
    public static final String GATEWAY_NODE_SERVICE = "/services";
    public static final String REQUEST_ATTRIBUTE_RULES_FACTS = "gateway.facts";

    public static final String PATH_MATCHER_ANY = "*";
    public static final String DEFAULT_MANAGEMENT_CONTEXT_PATH = "/management";
}
