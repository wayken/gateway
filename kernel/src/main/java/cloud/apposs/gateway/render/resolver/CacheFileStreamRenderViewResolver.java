package cloud.apposs.gateway.render.resolver;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.render.IRenderViewResolver;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.util.CachedFileStream;

public class CacheFileStreamRenderViewResolver implements IRenderViewResolver {
    @Override
    public void resolve(Zone zone, Route route, GatewayHttpRequest request, GatewayHttpResponse response, Object value) throws Exception {
        if (response.getContentType() == null) {
            String contentType = route.getContentType();
            String charset = route.getCharset();
            response.setContentType(contentType + "; charset=" + charset);
        }
        zone.getPluginSupport().postFilter(request, response, zone, route, value);
        try (CachedFileStream cachedFileStream = (CachedFileStream) value) {
            if (cachedFileStream.isInMemory()) {
                // 数据响应为内存数据，直接数据流输出
                response.write(cachedFileStream.getRawData(), true);
            } else {
                // 数据响应为文件流，直接文件流输出
                response.write(cachedFileStream.getRawFile(), true);
            }
        }
        zone.getPluginSupport().afterCompletion(request, response, zone, route);
        zone.getListenerSupport().onCompletion(request, response, zone, route);
    }
}
