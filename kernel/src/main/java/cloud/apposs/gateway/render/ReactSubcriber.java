package cloud.apposs.gateway.render;

import cloud.apposs.gateway.GatewayExceptionResolver;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.react.IoSubscriber;

import java.lang.reflect.InvocationTargetException;

public final class ReactSubcriber extends IoSubscriber<Object> {
    private final Zone zone;

    private final Route route;

    private final GatewayHttpRequest request;

    private final GatewayHttpResponse response;

    private final GatewayExceptionResolver resolver;

    public ReactSubcriber(Zone zone, Route route, GatewayHttpRequest request, GatewayHttpResponse response, GatewayExceptionResolver resolver) {
        this.zone = zone;
        this.route = route;
        this.request = request;
        this.response = response;
        this.resolver = resolver;
    }

    @Override
    public void onNext(Object value) throws Exception {
        IRenderViewResolver render = RenderViewResolverSupport.getRenderViewResolver(value.getClass());
        render.resolve(zone, route, request, response, value);
    }

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable cause) {
        // 如果是方法调用中有异常，需要获取的是真正的业务异常
        if (cause instanceof InvocationTargetException) {
            cause = ((InvocationTargetException) cause).getTargetException();
        }
        try {
            // 调用统一异常接口响应错误信息
            zone.getPluginSupport().afterCompletion(request, response, zone, route, cause);
            zone.getListenerSupport().onCompletion(request, response, zone, route, cause);
            if (resolver == null) {
                throw cause;
            }
            resolver.resolveException(request, response, cause);
        } catch (Throwable unkonw) {
            // 没有默认的异常处理器则直接终端输出异常
            cause.printStackTrace();
            unkonw.printStackTrace();
        }
    }
}
