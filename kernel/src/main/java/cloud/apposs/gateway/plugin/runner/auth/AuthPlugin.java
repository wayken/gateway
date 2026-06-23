package cloud.apposs.gateway.plugin.runner.auth;

import cloud.apposs.gateway.GatewayException;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.annotation.CommonPlugin;
import cloud.apposs.gateway.global.Global;
import cloud.apposs.gateway.global.auth.Auth;
import cloud.apposs.gateway.plugin.PluginAdapter;
import cloud.apposs.gateway.plugin.PluginResult;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.logger.Logger;
import cloud.apposs.react.React;
import cloud.apposs.util.HttpStatus;

/**
 * 服务认证者插件，需要与路由认证配合才可以使用
 * 参考：
 * <pre>
 *     https://apisix.incubator.apache.org/zh/docs/apisix/terminology/consumer/
 * </pre>
 */
@CommonPlugin
public class AuthPlugin extends PluginAdapter {
    public static final String NAME = "Auth";
    private static final int PLUGIN_PRIORITY = 1050;

    @Autowired
    private Global global;

    public AuthPlugin() {
        super(NAME);
    }

    @Override
    public int getPriority() {
        return PLUGIN_PRIORITY;
    }

    @Override
    public React<PluginResult> preFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) throws Exception {
        String authId = route.authId();
        if (authId == null) {
            return React.just(PluginResult.SUCCESS);
        }
        Auth auth = global.getAuth(authId);
        if (auth == null) {
            Logger.warn("AuthPlugin: AuthId " + authId + " Not Found for Route: " + route + ", Skipping");
            return React.just(PluginResult.SUCCESS);
        }
        return auth.auth(request, response, zone, route).map(result -> {
            if (!result) {
                throw new GatewayException(HttpStatus.HTTP_STATUS_401, "Route " + route + " Unauthorized By " + auth);
            }
            return PluginResult.SUCCESS;
        });
    }
}
