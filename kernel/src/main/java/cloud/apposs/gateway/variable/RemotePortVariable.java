package cloud.apposs.gateway.variable;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;

import java.net.InetSocketAddress;

/**
 * 请求远程端口，对应参数：$remote_port
 */
public class RemotePortVariable implements IVariable {
    @Override
    public String parse(GatewayHttpRequest request, GatewayHttpResponse response) {
        InetSocketAddress address = (InetSocketAddress) request.getRemoteAddr();
        return String.valueOf(address.getPort());
    }
}
