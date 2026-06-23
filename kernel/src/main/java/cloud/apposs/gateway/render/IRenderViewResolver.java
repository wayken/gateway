package cloud.apposs.gateway.render;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.zone.Zone;

public interface IRenderViewResolver {
    void resolve(Zone zone, Route route, GatewayHttpRequest request, GatewayHttpResponse response, Object value) throws Exception;
}
