package cloud.apposs.gateway.listener;

import cloud.apposs.gateway.GatewayConfig;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.plugin.Plugin;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.zone.Zone;

import java.util.EventListener;

/**
 * 网关请求监听器
 */
public interface Listener extends EventListener {
    /**
     * 插件初始化方法，用于初始化插件配置
     */
    Plugin initialize(Zone zone, GatewayConfig configiuration);

    /**
     * 整个请求处理完毕回调方法，无论请求逻辑处理有没有成功，可应用的场景如下：
     * <pre>
     *     1. 监听请求结束事件并添加到队列中进行请求数据入库
     *     2. 监听各自规则事件并触发短信、邮件通知回调
     * </pre>
     *
     * @param request   请求对象
     * @param response  响应对象，像登录验证不通过则需要输出重新登录信息
     * @param zone      当前命中的区域
     * @param throwable 如果业务调用产生了异常，则该值不为空，即表示业务处理逻辑出现了问题
     */
    void onCompletion(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route, Throwable throwable);
}
