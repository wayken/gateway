package cloud.apposs.gateway.rules;

/**
 * 网关默认规则引擎，用于处理网关的规则校验逻辑
 */
public class DefaultRulesEngine implements RulesEngine {
    @Override
    public boolean fire(Rule rule, Facts facts, Object... arguments) throws Exception {
        Rules rules = new Rules(rule);
        return fire(rules, facts, arguments);
    }

    @Override
    public boolean fire(Rules rules, Facts facts, Object... arguments) throws Exception {
        if (rules == null || rules.isEmpty()) {
            return true;
        }
        boolean result = true;
        for (Rule rule : rules) {
            result = rule.evaluate(facts, arguments);
            if (!result) {
                continue;
            }
            rule.execute(facts, arguments);
            if (rule.isSkipOnFirstAppliedRule()) {
                break;
            }
        }
        return result;
    }
}
