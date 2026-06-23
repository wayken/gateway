package cloud.apposs.gateway.plugin.runner.waf.system;

public class XssInjectRule extends TextInjectRule {
    @Override
    public String name() {
        return "XssInject";
    }

    @Override
    String getRuleFilePath() {
        return "rules/waf-xss-inject.rule";
    }
}
