package cloud.apposs.gateway.management;

import cloud.apposs.gateway.GatewayConfig;
import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.util.WebUtil;
import cloud.apposs.rest.*;

import java.util.Map;

/**
 * 网关管理服务路由，负责处理来自管理端的请求，包括健康检查、Prometheus监控、网关静态文件上传等
 */
public final class ManagementRouter {
    private final GatewayContext context;

    private final GatewayConfig config;

    private Restful<GatewayHttpRequest, GatewayHttpResponse> restful;

    public ManagementRouter(GatewayContext context, GatewayConfig config) throws Exception {
        this.context = context;
        this.config = config;
        RestConfig restConfig = handleInitRestConfig();
        this.restful = new Restful<>(restConfig);
        this.restful.initialize();
    }

    public void route(GatewayHttpRequest request, GatewayHttpResponse response, GatewayContext context) {
        restful.renderView(new ManagementHandlerProcess(), request, response);
    }

    private RestConfig handleInitRestConfig() {
        RestConfig restConfig = new RestConfig();
        String basePackage = ManagementRouter.class.getPackage().getName();
        restConfig.setBasePackage(basePackage);
        restConfig.setContextPath(config.getManagementContextPath());
        restConfig.setCharset(config.getCharset());
        restConfig.setHttpLogEnable(false);
        restConfig.setWorkerCount(1);
        return restConfig;
    }

    private class ManagementHandlerProcess implements IHandlerProcess<GatewayHttpRequest, GatewayHttpResponse> {
        @Override
        public String getRequestMethod(GatewayHttpRequest request, GatewayHttpResponse response) {
            return request.getMethod();
        }

        @Override
        public String getRequestPath(GatewayHttpRequest request, GatewayHttpResponse response) {
            return WebUtil.getRequestPath(request);
        }

        @Override
        public String getRequestHost(GatewayHttpRequest request, GatewayHttpResponse response) {
            return request.getRemoteHost();
        }

        @Override
        public void processVariable(GatewayHttpRequest request, GatewayHttpResponse response, Map<String, String> variables) {
        }

        @Override
        public void processHandler(GatewayHttpRequest request, GatewayHttpResponse response, Handler handler) {
            String produces = handler.getProduces();
            if (produces != null && !produces.isEmpty()) {
                response.setContentType(produces);
            }
        }

        @Override
        public IGuardProcess<GatewayHttpRequest, GatewayHttpResponse> getGuardProcess() {
            return null;
        }

        @Override
        public void markAsync(GatewayHttpRequest request, GatewayHttpResponse response) {
            // do nothing
        }
    }
}
