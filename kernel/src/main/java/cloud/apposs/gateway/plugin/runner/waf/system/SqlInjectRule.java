package cloud.apposs.gateway.plugin.runner.waf.system;

public class SqlInjectRule extends TextInjectRule {
    @Override
    public String name() {
        return "SqlInject";
    }

    @Override
    String getRuleFilePath() {
        return "rules/waf-sql-inject.rule";
    }
}
