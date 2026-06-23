package cloud.apposs.gateway.plugin.runner.limit;

import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.rules.Action;
import cloud.apposs.gateway.rules.Facts;
import cloud.apposs.util.HttpStatus;

public class LimitAction implements Action {
    @Override
    public void execute(Facts facts, Object... arguments) throws Exception {
        GatewayHttpResponse response = (GatewayHttpResponse) arguments[3];
        response.setStatus(HttpStatus.HTTP_STATUS_403.getCode());
        response.write("Too Many Requests", true);
    }
}
