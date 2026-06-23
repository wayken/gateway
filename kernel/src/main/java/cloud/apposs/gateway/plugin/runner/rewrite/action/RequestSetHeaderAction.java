package cloud.apposs.gateway.plugin.runner.rewrite.action;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.rules.Action;
import cloud.apposs.gateway.rules.Facts;

public class RequestSetHeaderAction implements Action {
    private final boolean dynamic;

    private final String headerName;

    private final String headerValue;

    public RequestSetHeaderAction(boolean dynamic, String headerName, String headerValue) {
        this.dynamic = dynamic;
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    @Override
    public void execute(Facts facts, Object... arguments) throws Exception {
        GatewayHttpRequest request = (GatewayHttpRequest) arguments[1];
        if (dynamic) {
            String value = facts.get(headerValue);
            if (value != null) {
                request.getHeaders().put(headerName, value);
            }
        } else {
            request.addHeader(headerName, headerValue);
        }
    }
}
