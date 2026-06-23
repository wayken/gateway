package cloud.apposs.gateway.plugin.runner.realip;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.annotation.CommonPlugin;
import cloud.apposs.gateway.plugin.Plugin;
import cloud.apposs.gateway.plugin.PluginAdapter;
import cloud.apposs.gateway.plugin.PluginResult;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.react.React;
import cloud.apposs.util.Param;

@CommonPlugin
public class RealIpPlugin extends PluginAdapter {
    public static final String NAME = "RealIp";
    private static final int PLUGIN_PRIORITY = 1020;

    public static final String KEY_ENABLE = "enable";
    public static final String KEY_SOURCE = "source";
    public static final String KEY_REAL_IP = "X-Real-IP";

    public RealIpPlugin() {
        super(NAME);
    }

    @Override
    public Plugin initialize(Zone zone, Param configiuration) {
        super.initialize(zone, configiuration);
        return this;
    }

    @Override
    public int getPriority() {
        return PLUGIN_PRIORITY;
    }

    @Override
    public React<PluginResult> preFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) throws Exception {
        return React.emitter(() -> {
            Param configuration = configiurationMapping.get(zone.getId());
            if (configuration == null) {
                return PluginResult.SUCCESS;
            }
            if (!configuration.getBoolean(KEY_ENABLE, false)) {
                return PluginResult.SUCCESS;
            }
            String source = configuration.getString(KEY_SOURCE);
            if (source == null) {
                return PluginResult.SUCCESS;
            }
            String realIp = request.getHeader(source, true);
            if (realIp != null) {
                request.getHeaders().put(KEY_REAL_IP, realIp);
            }
            return PluginResult.SUCCESS;
        });
    }
}
