package cloud.apposs.gateway.plugin;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.watch.IWatch;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.react.React;
import cloud.apposs.util.Param;

/**
 * 插件过滤器，用于对请求进行拦截处理，
 * 例如参数校验、登录验证、权限验证、日志输出、流量监控、协议转换、自定义注解等等，
 * 在配置中心的配置结构如下：
 * <pre>
 * {
 *   "name": "plugin-name",
 *   "class": "plugin-class",
 *   "config": {
 *     "key1": "value1",
 *     ...
 *   }
 * }
 * </pre>
 * 其中name为插件名称，class为插件类名，config为插件配置，key-value形式的配置，当class为空时则代表是网关内置插件
 */
public interface Plugin extends Comparable<Plugin> {
    String KEY_NAME = "name";
    String KEY_CLASS = "class";
    String KEY_METADATA = "metadata";

    /**
     * 当前插件名称，用于标识插件，注意插件列表中每个插件名称不能有重复
     */
    String name();

    /**
     * 插件初始化方法，用于初始化插件配置
     */
    Plugin initialize(Zone zone, Param configiuration);

    /**
     * 默认规则优先级
     */
    int DEFAULT_PRIORITY = Integer.MAX_VALUE - 1;

    /**
     * 规则的优先级，值越小优先级越高，0为最高优先级，默认Integer最大值减一
     */
    default int getPriority() {
        return DEFAULT_PRIORITY;
    }

    /**
     * 注册插件观察者，用于监听插件的配置变化
     *
     * @param zone    当前命中的区域
     * @param context 网关上下文
     * @param watcher 观察者
     */
    void registerWatcher(Zone zone, GatewayContext context, IWatch watcher) throws Exception;

    /**
     * 该方法在请求处理之前进行调用
     * 可实现登录/权限/自定义注解等拦截操作
     * 采用异步的方式实现，IO异步用okHttp，CPU异步用线程池
     *
     * @param  request  请求对象
     * @param  response 响应对象，像登录验证不通过则需要输出重新登录信息
     * @param  zone     当前命中的区域
     * @param  route    当前命中的路由
     * @return true/false 通过拦截验证验证返回true
     */
    React<PluginResult> preFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) throws Exception;

    /**
     * 该方法在请求处理之后数据发送之前进行调用
     * 可实现对响应数据的改造，如添加响应体、加密、压缩数据等
     *
     * @param request   请求对象
     * @param response  响应对象，像登录验证不通过则需要输出重新登录信息
     * @param zone      当前命中的区域
     * @param route     当前命中的路由
     * @Param value     请求响应数据
     */
    void postFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route, Object value) throws Exception;

    /**
     * 整个请求处理完毕回调方法，无论请求逻辑处理有没有成功，可应用的场景如下：
     * <pre>
     *     1. 记录请求日志和请求耗时
     *     2. 清理资源，如关闭数据库连接、释放锁等
     *     3. 监控统计，如记录请求成功次数、失败次数等
     *     4. 对响应结果进行统一处理，如加密、压缩等
     *     5. 改造响应数据，包括修改响应头、响应体等
     * </pre>
     *
     * @param request   请求对象
     * @param response  响应对象，像登录验证不通过则需要输出重新登录信息
     * @param zone      当前命中的区域
     * @param throwable 如果业务调用产生了异常，则该值不为空，即表示业务处理逻辑出现了问题
     */
    void afterCompletion(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route, Throwable throwable);

    /**
     * 销毁插件，释放资源
     */
    void destroy();
}
