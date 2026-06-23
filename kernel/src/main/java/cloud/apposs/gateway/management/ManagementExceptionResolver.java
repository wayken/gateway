package cloud.apposs.gateway.management;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.WebExceptionResolver;
import cloud.apposs.util.Errno;
import cloud.apposs.util.StandardResult;

@Component
public class ManagementExceptionResolver implements WebExceptionResolver<GatewayHttpRequest, GatewayHttpResponse> {
    @Override
    public Object resolveHandlerException(GatewayHttpRequest request, GatewayHttpResponse response, Throwable throwable) {
        return StandardResult.error(Errno.ERROR);
    }
}
