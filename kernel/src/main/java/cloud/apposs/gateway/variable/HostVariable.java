package cloud.apposs.gateway.variable;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;

/**
 * 请求远程主机，对应参数：$host
 */
public class HostVariable implements IVariable {
    @Override
    public String parse(GatewayHttpRequest request, GatewayHttpResponse response) {
        return request.getRemoteHost();
    }
}
