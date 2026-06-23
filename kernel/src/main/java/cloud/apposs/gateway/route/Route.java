package cloud.apposs.gateway.route;

import cloud.apposs.gateway.*;
import cloud.apposs.gateway.rules.Rule;
import cloud.apposs.gateway.rules.mvel.MVELRule;
import cloud.apposs.gateway.upstream.Upstream;
import cloud.apposs.react.React;
import cloud.apposs.util.HttpStatus;

import java.util.Arrays;

/**
 * 路由处理模块，主要负责请求路径和方法匹配，以及请求规则匹配，类似于Nginx的Location配置
 */
public class Route {
    /**
     * 路由ID
     */
    private final String id;

    /**
     * 请求的匹配路径，支持AntPath匹配
     */
    private final String path;

    /**
     * 请求的匹配方法列表，如果为空则匹配所有请求方法
     */
    private final Method[] methods;

    /**
     * 请求的匹配规则，为MVEL表达式
     */
    private final MVELRule rule;

    /**
     * 路由优先级，数字越小优先级越高
     */
    private final int priority;

    /**
     * 路由状态，0表示禁用，1表示启用
     */
    private final byte status;

    /**
     * 方法请求路径是否是正则表达式
     */
    private boolean pattern = false;

    /**
     * 响应输出CONTENT-TYPE
     */
    private String contentType = GatewayConstants.DEFAULT_CONTENT_TYPE;

    /**
     * 输出响应编码
     */
    private String charset = GatewayConstants.DEFAULT_CHARSET;

    /**
     * 路由上游ID，框架会根据此ID获取对应的上游进行转发
     */
    private String upstreamId = null;

    /**
     * 转发上游
     */
    private Upstream upstream = null;

    /**
     * 路由授权ID，框架会根据此ID获取对应的授权进行认证授权
     */
    private String authId = null;

    public Route(String id, String path, Method[] methods, String rule, int priority, byte status) {
        this.id = id;
        this.path = path;
        this.methods = methods;
        if (rule != null && !rule.trim().isEmpty()) {
            this.rule = new MVELRule(id).when(rule);
        } else {
            this.rule = null;
        }
        this.priority = priority;
        this.status = status;
    }

    /**
     * 处理HTTP路由请求，根据处理返回标志，上层ApplicationHandler会根据结果决定是否进行最后拦截器触发
     *
     * @return 异步处理结果
     */
    public React<?> route(GatewayHttpRequest request, GatewayHttpResponse response, GatewayContext context) throws Exception {
        // 获取当前请求路由的 upstream 进行转发
        if (upstream == null) {
            throw new GatewayException(HttpStatus.HTTP_STATUS_501, "No Upstream Setting For Route " + id);
        }
        return upstream.request(request, response, context);
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public Method[] getMethods() {
        return methods;
    }

    public Rule getRule() {
        return rule;
    }

    public int getPriority() {
        return priority;
    }

    public byte getStatus() {
        return status;
    }

    public boolean isEnable() {
        return status == RouteConstant.STATUS_ENABLE;
    }

    public boolean isPattern() {
        return pattern;
    }

    public void setPattern(boolean pattern) {
        this.pattern = pattern;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String upstreamId() {
        return upstreamId;
    }

    public Route upstreamId(String upstreamId) {
        this.upstreamId = upstreamId;
        return this;
    }

    public Route upstream(Upstream upstream) {
        this.upstream = upstream;
        return this;
    }

    public String authId() {
        return authId;
    }

    public Route authId(String authId) {
        this.authId = authId;
        return this;
    }

    /**
     * 关闭路由，释放资源，一般用于路由的热加载，包括更新、删除等操作
     */
    public void shutdown() {
        if (upstream != null && RouteParser.isManualUpstream(this)) {
            upstream.shutdown();
        }
    }

    @Override
    public String toString() {
        return "Route {" +
                "id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", status=" + status +
                ", methods=" + (methods == null ? "[]" : Arrays.toString(methods)) +
                ", rule=" + (rule != null) +
                ", pattern=" + pattern +
                ", upstreamId='" + upstreamId + '\'' +
                ", upstream=" + (upstream == null ? "NULL" : upstream.getName()) +
                ", authId='" + authId + '\'' +
                '}';
    }

    public enum Method {
        GET("GET"), POST("POST"), PUT("PUT"), DELETE("DELETE"),
        PATCH("PATCH"), HEAD("HEAD"), OPTIONS(("OPTIONS")), CONNECT("CONNECT"), TRACE("TRACE"), PURGE("PURGE");

        private final String value;

        public static final Method[] VALUES = values();

        Method(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        public boolean matched(String method) {
            return value.equalsIgnoreCase(method);
        }
    }
}
