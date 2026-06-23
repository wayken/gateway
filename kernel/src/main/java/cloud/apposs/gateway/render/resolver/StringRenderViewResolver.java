package cloud.apposs.gateway.render.resolver;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.render.IRenderViewResolver;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.zone.Zone;

public class StringRenderViewResolver implements IRenderViewResolver {
    @Override
    public void resolve(Zone zone, Route route, GatewayHttpRequest request, GatewayHttpResponse response, Object value) throws Exception {
        if (response.getContentType() == null) {
            String contentType = route.getContentType();
            String charset = route.getCharset();
            response.setContentType(contentType + "; charset=" + charset);
        }
        zone.getPluginSupport().postFilter(request, response, zone, route, value);
        // 数据响应为普通文本，直接文本内容输出
        response.write(value.toString(), true);
        zone.getPluginSupport().afterCompletion(request, response, zone, route);
        zone.getListenerSupport().onCompletion(request, response, zone, route);
    }
}
