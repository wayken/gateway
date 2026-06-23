package cloud.apposs.gateway.plugin.runner.cache.action;

import cloud.apposs.gateway.GatewayConfig;
import cloud.apposs.gateway.rules.Action;
import cloud.apposs.gateway.rules.Facts;
import cloud.apposs.util.Param;

public class CacheControlAction implements Action {
    private String dataSourcePath;

    public CacheControlAction(GatewayConfig config, Param parameters) {
        this.dataSourcePath = config.getDataSourcePath();
    }

    @Override
    public void execute(Facts facts, Object... arguments) throws Exception {
    }
}
