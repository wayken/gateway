package cloud.apposs.gateway.plugin.runner.cache;

import cloud.apposs.util.Encryptor;

import java.io.File;

public final class CacheConstant {
    /** 规则配置路径 */
    public static final String DEFAULT_CACHE_PATH = "/cache";

    /** 规则配置项 */
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_RULE = "rule";
    public static final String KEY_STATUS = "status";
    public static final String KEY_ROUTES = "routes";
    public static final String KEY_PRIORITY = "priority";
    public static final String KEY_REMARK = "remark";
    public static final String KEY_ACTION = "action";

    /** 缓存规则动作 */
    public static class Action {
        // 配置项
        public static final String KEY_TYPE = "type";
        public static final String KEY_EDGE = "edge";
        public static final String KEY_TTL = "ttl";

        // 动作类型
        public static final String CACHE_CONTROL = "cache-control";
        public static final String CACHE_FORCE = "cache-force";
    }

    public static String REQUEST_ATTRIBUTE_CACHEABLE = "Cache:Cacheable";

    /**
     * 获取缓存路径，规则为${host}/${url}:${method}
     */
    public static String getCachePath(String dataSourcePath, String host, String url, String method) {
        String hostSignature = Encryptor.md5(host);
        String urlSignature = Encryptor.md5(url + ":" + method);
        return dataSourcePath + File.separator + "cache" + File.separator + hostSignature + File.separator + urlSignature;
    }
    public static String getCacheHeaderPath(String path) {
        return path + File.separator + "header";
    }
    public static String getCacheContentPath(String path) {
        return path + File.separator + "content";
    }

    /** 缓存响应Header */
    public static String HEADER_CACHE_STATUS = "X-GW-Cache-Status";
    public static class CacheStatus {
        public static final String HIT = "HIT";
        public static final String MISS = "MISS";
        public static final String EXPIRED = "EXPIRED";
        public static final String ERROR = "ERROR";
    }
}
