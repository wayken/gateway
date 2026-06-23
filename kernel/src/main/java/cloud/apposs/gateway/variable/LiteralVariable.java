package cloud.apposs.gateway.variable;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;

public class LiteralVariable implements IVariable {
    private final String literal;

    public LiteralVariable(String literal) {
        this.literal = literal;
    }

    @Override
    public String parse(GatewayHttpRequest request, GatewayHttpResponse response) {
        return literal;
    }
}
