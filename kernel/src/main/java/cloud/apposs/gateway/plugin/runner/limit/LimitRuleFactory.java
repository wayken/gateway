package cloud.apposs.gateway.plugin.runner.limit;

import cloud.apposs.cache.CacheManager;
import cloud.apposs.gateway.plugin.runner.limit.rule.LimitCookieRule;
import cloud.apposs.gateway.plugin.runner.limit.rule.LimitJsRule;
import cloud.apposs.gateway.plugin.runner.limit.rule.LimitRule;
import cloud.apposs.gateway.plugin.runner.waf.WafConstant;

import java.util.List;

public final class LimitRuleFactory {
    public static LimitRule getRule(String actionType, String name, int status, int priority,
            List<String> matchedRuleIds, String resource, int burst, String burstUnit, CacheManager cache) {
        if (WafConstant.Action.COOKIE_CHALLENGE.equals(actionType)) {
            return new LimitCookieRule(name, status, priority, matchedRuleIds, resource, burst, burstUnit, cache);
        } else if (WafConstant.Action.SKIP.equals(actionType)) {
            return new LimitJsRule(name, status, priority, matchedRuleIds, resource, burst, burstUnit, cache);
        }
        return new LimitRule(cache, name, status, priority, matchedRuleIds, resource, burst, burstUnit);
    }
}
