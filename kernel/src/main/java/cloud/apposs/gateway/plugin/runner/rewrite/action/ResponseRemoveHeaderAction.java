package cloud.apposs.gateway.plugin.runner.rewrite.action;

import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.rules.Action;
import cloud.apposs.gateway.rules.Facts;

/**
 * 移除响应头
 */
public class ResponseRemoveHeaderAction implements Action {
    private final String header;

    public ResponseRemoveHeaderAction(String header) {
        this.header = header;
    }

    @Override
    public void execute(Facts facts, Object... arguments) throws Exception {
        GatewayHttpResponse response = (GatewayHttpResponse) arguments[2];
        response.removeHeader(header);
    }
}
