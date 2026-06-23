package cloud.apposs.gateway.plugin.runner.limit.rule;

import cloud.apposs.cache.CacheManager;
import cloud.apposs.gateway.plugin.runner.limit.LimitConstant;
import cloud.apposs.gateway.rules.Facts;
import cloud.apposs.gateway.rules.http.HttpMVELRule;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.guard.Guard;
import cloud.apposs.guard.ResourceToken;
import cloud.apposs.guard.exception.BlockException;
import cloud.apposs.guard.slot.limitkey.LimitKeyMetric;
import cloud.apposs.guard.slot.limitkey.LimitKeySlot;
import cloud.apposs.guard.slot.limitkey.rule.LimitKeyRule;
import cloud.apposs.guard.slot.limitkey.rule.LimitKeyRuleManager;
import cloud.apposs.util.Param;

import java.util.List;

public class LimitRule extends HttpMVELRule {
    private static final String CACHE_KEY_PREFIX = "Sys:Limit:";

    private final CacheManager cache;

    /** 特征匹配，支持多个特征合并，如`http.path`、`http.method`等 */
    private List<Param> limitKeys;

    /** 是否可跳过，一般是是在匹配到规则后续动作中有`跳过`、`记录`等操作 */
    private boolean skippable;

    /**
     * 缓存限流阻断时长，单位为毫秒，默认为0，即不阻断
     * 如果为>0则代表后续请求会先进入缓存限流，在指定时间内不会再次进入限流匹配规则
     */
    private int duration = 0;

    /** 缓存限流单位，默认为秒 */
    private String durationUnit = LimitConstant.Action.TIME_UNIT_SECOND;

    public LimitRule(CacheManager cache, String name, int status, int priority,
                     List<String> matchedRuleIds, String resource, int burst, String burstUnit) {
        super(name, status, priority, matchedRuleIds);
        this.cache = cache;
        LimitKeyRule rule = new LimitKeyRule(false);
        rule.setResource(resource);
        rule.setThreshold(burst);
        int burstDuration = getTimeDurationInMillisecond(1, burstUnit);
        LimitKeyMetric metric = new LimitKeyMetric(burstDuration);
        LimitKeySlot.replaceMetric(resource, metric);
        LimitKeyRuleManager.replaceRule(rule);
    }

    public void setLimitKeys(List<Param> limitKeys) {
        this.limitKeys = limitKeys;
    }

    public void setSkippable(boolean skippable) {
        this.skippable = skippable;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setDurationUnit(String durationUnit) {
        this.durationUnit = durationUnit;
    }

    @Override
    public boolean evaluate(Facts facts, Object... arguments) {
        boolean result = super.evaluate(facts, arguments);
        if (!result) {
            return false;
        }
        if (skippable) {
            return false;
        }
        // 如果资源限流匹配不到规则则跳过不再继续执行，否则开始进入资源限流
        ResourceToken token = null;
        String limitKey = getLimitKey(limitKeys, facts);
        try {
            // 如果缓存中存在黑名单则直接返回限流
            if (cache.exist(CACHE_KEY_PREFIX + limitKey)) {
                return true;
            }
            // 进入资源限流链路
            Zone zone = (Zone) arguments[0];
            token = Guard.entry(zone.getId(), limitKey);
            return false;
        } catch (BlockException e) {
            // 触发限流，如果配置了封禁行为，则将资源限流的KEY加入黑名单缓存一段时间，后续不再触发限流规则
            if (duration > 0) {
                cache.put(CACHE_KEY_PREFIX + limitKey, true);
                cache.expire(CACHE_KEY_PREFIX + limitKey, getTimeDurationInMillisecond(duration, durationUnit));
            }
            return true;
        } finally {
            if (token != null) {
                token.exit();
            }
        }
    }

    private static String getLimitKey(List<Param> limitKeys, Facts facts) {
        StringBuilder limitKeyBuilder = new StringBuilder();
        for (Param limitKey : limitKeys) {
            String key = limitKey.getString(LimitConstant.KEY_RESOURCE_KEY);
            String value = limitKey.getString(LimitConstant.KEY_RESOURCE_VALUE);
            String factValue = null;
            if (value == null) {
                factValue = facts.getValue(key);
            } else {
                factValue = facts.getValue(key + "[" + value + "]");
            }
            limitKeyBuilder.append(factValue);
        }
        return limitKeyBuilder.toString();
    }

    private static int getTimeDurationInMillisecond(int time, String unit) {
        if (LimitConstant.Action.TIME_UNIT_SECOND.equals(unit)) {
            return time * 1000;
        } else if (LimitConstant.Action.TIME_UNIT_MINUTE.equals(unit)) {
            return time * 60 * 1000;
        } else if (LimitConstant.Action.TIME_UNIT_HOUR.equals(unit)) {
            return time * 60 * 60 * 1000;
        } else if (LimitConstant.Action.TIME_UNIT_DAY.equals(unit)) {
            return time * 60 * 60 * 24 * 1000;
        }
        return time;
    }
}
