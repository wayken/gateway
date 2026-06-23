package cloud.apposs.gateway.variable;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;

import java.net.InetSocketAddress;

/**
 * 请求远程地址，对应参数：$remote_addr
 */
public class RemoteAddressVariable implements IVariable {
    @Override
    public String parse(GatewayHttpRequest request, GatewayHttpResponse response) {
        InetSocketAddress address = (InetSocketAddress) request.getRemoteAddr();
        return address.getHostString();
    }
}
