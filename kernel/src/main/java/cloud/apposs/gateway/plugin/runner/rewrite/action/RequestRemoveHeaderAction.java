package cloud.apposs.gateway.plugin.runner.rewrite.action;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.rules.Action;
import cloud.apposs.gateway.rules.Facts;

/**
 * 移除请求头
 */
public class RequestRemoveHeaderAction implements Action {
    private final String header;

    public RequestRemoveHeaderAction(String header) {
        this.header = header;
    }

    @Override
    public void execute(Facts facts, Object... arguments) throws Exception {
        GatewayHttpRequest request = (GatewayHttpRequest) arguments[1];
        request.getHeaders().remove(header);
    }
}
