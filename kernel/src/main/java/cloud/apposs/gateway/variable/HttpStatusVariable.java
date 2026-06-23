package cloud.apposs.gateway.variable;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;

/**
 * 请求响应状态码，对应参数：$status
 */
public class HttpStatusVariable implements IVariable {
    @Override
    public String parse(GatewayHttpRequest request, GatewayHttpResponse response) {
        return String.valueOf(response.getStatus());
    }
}
