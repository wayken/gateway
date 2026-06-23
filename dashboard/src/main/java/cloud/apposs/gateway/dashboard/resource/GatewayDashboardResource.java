package cloud.apposs.gateway.dashboard.resource;

import cloud.apposs.bootor.buildin.ResourceAction;
import cloud.apposs.rest.annotation.RestAction;

@RestAction
public class GatewayDashboardResource extends ResourceAction {
    @Override
    public String[] getStaticPath() {
        return new String[] {
                "classpath:static"
        };
    }
}
