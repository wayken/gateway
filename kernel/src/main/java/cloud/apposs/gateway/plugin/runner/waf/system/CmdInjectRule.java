package cloud.apposs.gateway.plugin.runner.waf.system;

public class CmdInjectRule extends TextInjectRule {
    @Override
    public String name() {
        return "CmdInject";
    }

    @Override
    String getRuleFilePath() {
        return "rules/waf-cmd-inject.rule";
    }
}
