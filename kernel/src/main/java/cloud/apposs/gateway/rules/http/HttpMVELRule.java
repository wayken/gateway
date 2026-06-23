package cloud.apposs.gateway.rules.http;

import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.rules.Facts;
import cloud.apposs.gateway.rules.Rule;
import cloud.apposs.gateway.rules.mvel.MVELRule;
import org.mvel2.ParserContext;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP请求规则，基于MVEL规则之上，用于处理HTTP请求的规则校验逻辑，包括
 * <pre>
 * 1. 判断当前请求是否匹配当前路由ID，如果不匹配则直接返回false
 * </pre>
 */
public class HttpMVELRule extends MVELRule {
    private int status = 1;

    private List<String> matchedRuleIds = new ArrayList<>();

    public HttpMVELRule(String name, int status, List<String> matchedRuleIds) {
        this(name, status, Rule.DEFAULT_PRIORITY, matchedRuleIds);
    }

    public HttpMVELRule(String name, int status, int priority, List<String> matchedRuleIds) {
        super(name);
        this.status = status;
        this.priority = priority;
        if (matchedRuleIds != null) {
            this.matchedRuleIds.addAll(matchedRuleIds);
        }
    }

    public HttpMVELRule(ParserContext parserContext) {
        super(parserContext);
    }

    public boolean isEnable() {
        return status == 1;
    }

    public void addMatchedRuleId(String ruleId) {
        matchedRuleIds.add(ruleId);
    }

    public void addMatchedRuleIds(List<String> ruleIds) {
        matchedRuleIds.addAll(ruleIds);
    }

    @Override
    public boolean evaluate(Facts facts, Object... arguments) {
        // 判断当前规则是否匹配当前请求的路由，如果为空则不做匹配校验，直接用MVEL规则校验
        if (!matchedRuleIds.isEmpty() && arguments.length > 0 && arguments[1] instanceof Route) {
            Route route = (Route) arguments[1];
            if (!matchedRuleIds.contains(route.getId())) {
                return false;
            }
        }
        if (!isEnable()) {
            return false;
        }
        // 该路由已经匹配过，再进行MVEL规则校验
        return super.evaluate(facts, arguments);
    }

    @Override
    public String toString() {
        return "HttpMVELRule{" +
                "name='" + name + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", matchedRuleIds=" + matchedRuleIds +
                '}';
    }
}
