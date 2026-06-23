package cloud.apposs.gateway.plugin.runner.redirect.rule;

import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.plugin.runner.redirect.RedirectConstant;
import cloud.apposs.gateway.rules.Facts;
import cloud.apposs.gateway.rules.http.HttpMVELRule;
import cloud.apposs.util.Param;

import java.util.List;

/**
 * 基于自定义MVEL筛选表达式的请求重定向规则
 */
public class RedirectMVELRule extends HttpMVELRule {
    private final String redirectUrl;

    private final int redirectStatus;

    public RedirectMVELRule(String name, int status, List<String> matchedRuleIds, String ruleData, Param parameters) {
        super(name, status, matchedRuleIds);
        this.redirectUrl = parameters.getString(RedirectConstant.KEY_ACTION_REDIRECT_URL);
        this.redirectStatus = parameters.getInt(RedirectConstant.KEY_ACTION_REDIRECT_STATUS, 301);
        this.when(ruleData);
    }

    @Override
    public void execute(Facts facts, Object... arguments) throws Exception {
        GatewayHttpResponse response = (GatewayHttpResponse) arguments[2];
        response.setStatus(redirectStatus);
        response.putHeader("Location", redirectUrl);
    }
}
