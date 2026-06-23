package cloud.apposs.gateway.plugin.runner.waf.system;

import cloud.apposs.gateway.GatewayConstants;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.rules.DefaultRulesEngine;
import cloud.apposs.gateway.rules.Facts;
import cloud.apposs.gateway.rules.Rule;
import cloud.apposs.gateway.rules.RulesEngine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class TextInjectRule implements WafSystemRule {
    /** 自定义匹配规则 */
    protected Rule rule;
    private final RulesEngine ruleEngine = new DefaultRulesEngine();

    /** 是否启用规则 */
    protected boolean enable = true;

    /** 预编译规则正则表达式 */
    protected final List<Pattern> patterns = new ArrayList<>();

    public TextInjectRule() {
        // 读取项目resources目录下的rules/waf-cmd-inject.rule文件，加载规则
        // 文件中的规则格式为正则表达式，一行一个规则
        String ruleFilePath = getRuleFilePath();
        InputStream buffer = TextInjectRule.class.getClassLoader().getResourceAsStream(ruleFilePath);
        if (buffer == null) {
            throw new RuntimeException("Load WAF " + name() + " Rule Error, Rule File " + ruleFilePath + " Not Found");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(buffer, StandardCharsets.UTF_8));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                patterns.add(Pattern.compile(line));
            }
        } catch (Exception e) {
            throw new RuntimeException("Load WAF " + name() + " Rule Error", e);
        }
    }

    @Override
    public boolean match(GatewayHttpRequest request, GatewayHttpResponse response) throws Exception {
        // 判断是否启用规则，如果未启用则直接通过
        if (!enable) {
            return true;
        }
        // 判断是否匹配过滤规则，如果不匹配则表示不需要进行规则过滤，直接通过
        if (rule != null) {
            Facts facts = (Facts) request.getAttribute(GatewayConstants.REQUEST_ATTRIBUTE_RULES_FACTS);
            boolean matched = ruleEngine.fire(rule, facts);
            if (!matched) {
                return true;
            }
        }
        // 开始匹配规则，如果匹配则表示请求参数中存在注入风险，需要进行拦截
        Map<String, String> parameters = request.getParameters();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String value = entry.getValue();
            for (Pattern pattern : patterns) {
                if (pattern.matcher(value).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    public void setRule(Rule rule) {
        this.rule = rule;
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    @Override
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * 规则文件
     */
    abstract String getRuleFilePath();
}
