package cloud.apposs.gateway.plugin.runner.waf.rule;

import cloud.apposs.gateway.rules.Facts;
import cloud.apposs.gateway.rules.http.HttpMVELRule;

import java.util.List;

public class WafRule extends HttpMVELRule {
    private final boolean skippable;

    public WafRule(String name, int status, int priority, List<String> matchedRuleIds, boolean skippable) {
        super(name, status, priority, matchedRuleIds);
        this.skippable = skippable;
    }

    @Override
    public boolean evaluate(Facts facts, Object... arguments) {
        boolean result = super.evaluate(facts, arguments);
        if (!result) {
            return false;
        }
        // 如果规则不可跳过，则返回true，否则返回false
        // 一般是是在匹配到规则后续动作中有`跳过`、`记录`等操作
        return !skippable;
    }
}
