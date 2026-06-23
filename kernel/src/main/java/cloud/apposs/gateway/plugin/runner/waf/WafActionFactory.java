package cloud.apposs.gateway.plugin.runner.waf;

import cloud.apposs.gateway.plugin.runner.waf.action.WafBlockAction;
import cloud.apposs.gateway.plugin.runner.waf.action.WafSkipAction;
import cloud.apposs.gateway.rules.Action;
import cloud.apposs.util.Param;

public final class WafActionFactory {
    public static Action getRuleAction(String actionType, Param parameters) {
        if (WafConstant.Action.BLOCK.equals(actionType)) {
            return new WafBlockAction(parameters);
        } else if (WafConstant.Action.SKIP.equals(actionType)) {
            return new WafSkipAction();
        }
        return new WafBlockAction(parameters);
    }
}
