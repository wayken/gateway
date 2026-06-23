package cloud.apposs.gateway.plugin;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.watch.IWatch;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.react.React;
import cloud.apposs.util.Param;

import java.util.HashMap;
import java.util.Map;

public class PluginAdapter implements Plugin {
    protected final String name;

    protected Map<String, Param> configiurationMapping = new HashMap<>();

    public PluginAdapter(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Plugin initialize(Zone zone, Param configiuration) {
        configiurationMapping.put(zone.getId(), configiuration);
        return this;
    }

    @Override
    public void registerWatcher(Zone zone, GatewayContext context, IWatch watcher) throws Exception {
        // Do nothing
    }

    @Override
    public React<PluginResult> preFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) throws Exception {
        return React.just(PluginResult.SUCCESS);
    }

    @Override
    public void postFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route, Object value) throws Exception {
    }

    @Override
    public void afterCompletion(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route, Throwable throwable) {
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Plugin)) {
            return false;
        }
        return name.equals(((Plugin) obj).name());
    }

    @Override
    public int compareTo(Plugin o) {
        return Integer.compare(getPriority(), o.getPriority());
    }
}
