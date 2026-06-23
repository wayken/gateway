package cloud.apposs.gateway.plugin.runner.rewrite.action;

import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.rules.Action;
import cloud.apposs.gateway.rules.Facts;

public class ResponseSetHeaderAction implements Action {
    private final boolean dynamic;

    private final String headerName;

    private final String headerValue;

    public ResponseSetHeaderAction(boolean dynamic, String headerName, String headerValue) {
        this.dynamic = dynamic;
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    @Override
    public void execute(Facts facts, Object... arguments) throws Exception {
        GatewayHttpResponse response = (GatewayHttpResponse) arguments[2];
        if (dynamic) {
            String value = facts.getValue(headerValue);
            if (value != null) {
                response.putHeader(headerName, value);
            }
        } else {
            response.putHeader(headerName, headerValue);
        }
    }
}
