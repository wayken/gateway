package cloud.apposs.gateway.plugin.runner.redirect;

import cloud.apposs.gateway.plugin.runner.redirect.rule.RedirectMVELRule;
import cloud.apposs.gateway.plugin.runner.redirect.rule.RedirectPatternRule;
import cloud.apposs.gateway.plugin.runner.rewrite.RewriteConstant;
import cloud.apposs.gateway.rules.Rule;
import cloud.apposs.util.Param;

import java.util.List;

public final class RedirectRuleFactory {
    public static Rule getRule(String ruleType, String name, int status, List<String> matchedRuleIds, String ruleData, Param parameters) {
        if (RewriteConstant.RULE_TYPE_PATTERN.equals(ruleType)) {
            return new RedirectPatternRule(name, status, matchedRuleIds, ruleData, parameters);
        }
        return new RedirectMVELRule(name, status, matchedRuleIds, ruleData, parameters);
    }
}
