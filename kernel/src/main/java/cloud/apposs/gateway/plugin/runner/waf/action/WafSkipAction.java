package cloud.apposs.gateway.plugin.runner.waf.action;

import cloud.apposs.gateway.rules.Action;
import cloud.apposs.gateway.rules.Facts;

public class WafSkipAction implements Action {
    @Override
    public void execute(Facts facts, Object... arguments) throws Exception {
        // do nothing
    }
}
