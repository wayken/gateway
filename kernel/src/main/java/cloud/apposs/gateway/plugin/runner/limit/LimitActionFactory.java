package cloud.apposs.gateway.plugin.runner.limit;

import cloud.apposs.gateway.plugin.runner.limit.action.LimitBlockAction;
import cloud.apposs.gateway.plugin.runner.limit.action.LimitSkipAction;
import cloud.apposs.gateway.plugin.runner.waf.WafConstant;
import cloud.apposs.gateway.rules.Action;
import cloud.apposs.util.Param;

public final class LimitActionFactory {
    public static Action getRuleAction(String actionType, Param parameters) {
        if (WafConstant.Action.BLOCK.equals(actionType)) {
            return new LimitBlockAction(parameters);
        } else if (WafConstant.Action.SKIP.equals(actionType)) {
            return new LimitSkipAction();
        }
        return new LimitBlockAction(parameters);
    }
}
