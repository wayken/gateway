package cloud.apposs.gateway.watch;

import cloud.apposs.gateway.GatewayContext;

public abstract class AbstractWatch implements IWatch {
    protected final GatewayContext context;

    protected AbstractWatch(GatewayContext context) {
        this.context = context;
    }
}
