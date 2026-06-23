package cloud.apposs.gateway.render.resolver;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.render.IRenderViewResolver;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.util.MediaType;

import java.io.File;

public class FileRenderViewResolver implements IRenderViewResolver {
    @Override
    public void resolve(Zone zone, Route route, GatewayHttpRequest request, GatewayHttpResponse response, Object value) throws Exception {
        File file = (File) value;
        if (response.getContentType() == null) {
            String contentType = route.getContentType();
            // 获取文件类型
            String fileName = file.getName();
            String fileExtension = getFileExtension(fileName);
            MediaType mediaType = MediaType.getMediaTypeByFileExtension(fileExtension);
            if (mediaType != null) {
                contentType = mediaType.value();
            }
            String charset = route.getCharset();
            response.setContentType(contentType + "; charset=" + charset);
        }
        zone.getPluginSupport().postFilter(request, response, zone, route, value);
        // 数据响应为文件流，直接文件零拷贝输出
        response.write(file, true);
        zone.getPluginSupport().afterCompletion(request, response, zone, route);
        zone.getListenerSupport().onCompletion(request, response, zone, route);
    }

    private static String getFileExtension(String filePath) {
        if (filePath == null) {
            return null;
        }
        int index = filePath.lastIndexOf(".");
        if (index == -1) {
            return null;
        }
        return filePath.substring(index + 1);
    }
}
