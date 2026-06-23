package cloud.apposs.gateway.variable;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.util.WebUtil;

/**
 * 请求远程方法+URL，对应参数：$request
 */
public class RequestVariable implements IVariable {
    @Override
    public String parse(GatewayHttpRequest request, GatewayHttpResponse response) {
        String method = request.getMethod().toUpperCase();
        String uri = WebUtil.getRequestPath(request);
        return method + " " + uri;
    }
}
