package cloud.apposs.gateway.plugin.runner.waf;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.plugin.runner.waf.system.CmdInjectRule;
import cloud.apposs.gateway.plugin.runner.waf.system.SqlInjectRule;
import cloud.apposs.gateway.plugin.runner.waf.system.WafSystemRule;
import cloud.apposs.gateway.plugin.runner.waf.system.XssInjectRule;
import cloud.apposs.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * WAF系统规则过滤器
 */
public class WafRuleFilter {
    private final List<WafSystemRule> rules = new ArrayList<WafSystemRule>();

    public WafRuleFilter() {
        addRule(new CmdInjectRule());
        addRule(new XssInjectRule());
        addRule(new SqlInjectRule());
    }

    public void addRule(WafSystemRule rule) {
        boolean success = rules.add(rule);
        Logger.info("Add System WAF Rule %s %s ", rule.name(), success ? "Success" : "Failed");
    }

    public WafSystemRule getRule(String name) {
        for (WafSystemRule rule : rules) {
            if (rule.name().equals(name)) {
                return rule;
            }
        }
        return null;
    }

    public void removeRule(String name) {
        WafSystemRule removeRule = getRule(name);
        if (removeRule == null) {
            return;
        }
        boolean success = rules.remove(removeRule);
        Logger.info("Remove System WAF Rule %s %s ", removeRule.name(), success ? "Success" : "Failed");
    }

    public boolean match(GatewayHttpRequest request, GatewayHttpResponse response) throws Exception {
        if (rules.isEmpty()) {
            return true;
        }
        for (WafSystemRule rule : rules) {
            if (rule.match(request, response)) {
                return true;
            }
        }
        return false;
    }
}
