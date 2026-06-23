package cloud.apposs.gateway.variable;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;

/**
 * 请求头部，对应参数：$http_xxx_xxx
 */
public class HttpHeaderVariable implements IVariable {
    private final String header;

    public HttpHeaderVariable(String header) {
        this.header = header;
    }

    @Override
    public String parse(GatewayHttpRequest request, GatewayHttpResponse response) {
        String value = request.getHeader(header);
        if (value == null || value.trim().equals("")) {
            return "-";
        }
        return value;
    }
}
