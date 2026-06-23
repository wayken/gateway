package cloud.apposs.gateway.plugin.runner.redirect.rule;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.plugin.runner.redirect.RedirectConstant;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.rules.BasicRule;
import cloud.apposs.gateway.rules.Facts;
import cloud.apposs.util.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于通配符模式的请求重定向规则
 */
public class RedirectPatternRule extends BasicRule {
    public static final String REQUEST_ATTRIBUTE_PATTERH = "Redirect:Pattern:Attribute";

    private int status = 1;

    private Pattern pattern;

    private List<String> matchedRuleIds = new ArrayList<>();

    private final String redirectUrl;

    private final int redirectStatus;

    public RedirectPatternRule(String name, int status, List<String> matchedRuleIds, String ruleData, Param parameters) {
        super(name);
        this.status = status;
        if (matchedRuleIds != null) {
            this.matchedRuleIds.addAll(matchedRuleIds);
        }
        this.pattern = Pattern.compile(ruleData);
        this.redirectUrl = parameters.getString(RedirectConstant.KEY_ACTION_REDIRECT_URL);
        this.redirectStatus = parameters.getInt(RedirectConstant.KEY_ACTION_REDIRECT_STATUS, 301);
    }

    public boolean isEnable() {
        return status == 1;
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
        // 进行正则匹配
        GatewayHttpRequest request = (GatewayHttpRequest) arguments[1];
        String url = request.getUrl();
        Matcher matcher = pattern.matcher(url);
        if (!matcher.matches()) {
            return false;
        }
        request.setAttribute(REQUEST_ATTRIBUTE_PATTERH, matcher);
        return true;
    }

    @Override
    public void execute(Facts facts, Object... arguments) throws Exception {
        GatewayHttpRequest request = (GatewayHttpRequest) arguments[1];
        Matcher matcher = (Matcher) request.getAttribute(REQUEST_ATTRIBUTE_PATTERH);
        if (matcher == null) {
            throw new IllegalStateException("RedirectPatternRule execute error: matcher is null");
        }
        GatewayHttpResponse response = (GatewayHttpResponse) arguments[2];
        String result = matcher.replaceAll(redirectUrl);
        response.setStatus(redirectStatus);
        response.putHeader("Location", result);
    }
}
