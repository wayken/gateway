package cloud.apposs.gateway.plugin.runner.waf;

import cloud.apposs.cache.CacheManager;
import cloud.apposs.gateway.plugin.runner.waf.rule.WafCookieRule;
import cloud.apposs.gateway.plugin.runner.waf.rule.WafJsRule;
import cloud.apposs.gateway.plugin.runner.waf.rule.WafRule;
import cloud.apposs.gateway.rules.mvel.MVELRule;

import java.util.List;

public final class WafRuleFactory {
    public static MVELRule getRule(String actionType, String name, int status, int priority, List<String> matchedRuleIds, boolean skippable, CacheManager cache) {
        if (WafConstant.Action.COOKIE_CHALLENGE.equals(actionType)) {
            return new WafCookieRule(name, status, priority, matchedRuleIds, skippable, cache);
        } else if (WafConstant.Action.SKIP.equals(actionType)) {
            return new WafJsRule(name, status, priority, matchedRuleIds, skippable, cache);
        }
        return new WafRule(name, status, priority, matchedRuleIds, skippable);
    }
}
