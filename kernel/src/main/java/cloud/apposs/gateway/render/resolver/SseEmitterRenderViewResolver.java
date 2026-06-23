package cloud.apposs.gateway.render.resolver;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.render.IRenderViewResolver;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.util.SseEmitter;

public class SseEmitterRenderViewResolver implements IRenderViewResolver {
    @Override
    public void resolve(Zone zone, Route route, GatewayHttpRequest request, GatewayHttpResponse response, Object value) throws Exception {
        SseEmitter emitter = (SseEmitter) value;
        boolean done = emitter.isDone();
        if (done) {
            zone.getPluginSupport().postFilter(request, response, zone, route, value);
        }
        // 数据流为SSE响应流
        response.write(emitter, true);
        if (done) {
            zone.getPluginSupport().afterCompletion(request, response, zone, route);
            zone.getListenerSupport().onCompletion(request, response, zone, route);
        }
    }
}
