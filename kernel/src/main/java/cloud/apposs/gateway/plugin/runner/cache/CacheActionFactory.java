package cloud.apposs.gateway.plugin.runner.cache;

import cloud.apposs.gateway.GatewayConfig;
import cloud.apposs.gateway.plugin.runner.cache.action.CacheControlAction;
import cloud.apposs.gateway.plugin.runner.cache.action.CacheForceAction;
import cloud.apposs.gateway.rules.Action;
import cloud.apposs.util.Param;

public final class CacheActionFactory {
    public static Action getRuleAction(String actionType, GatewayConfig config, Param parameters) {
        if (CacheConstant.Action.CACHE_CONTROL.equals(actionType)) {
            return new CacheControlAction(config, parameters);
        } else if (CacheConstant.Action.CACHE_FORCE.equals(actionType)) {
            return new CacheForceAction(config, parameters);
        }
        return new CacheForceAction(config, parameters);
    }
}
