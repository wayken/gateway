package cloud.apposs.gateway.variable;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;

public interface IVariable {
    /**
     * 解析对应的配置参数，示例：$remote_addr/$remote_port/$http_user_agent等
     */
    String parse(GatewayHttpRequest request, GatewayHttpResponse response);
}
