package cloud.apposs.gateway.rules;

/**
 * 网关规则引擎，用于处理网关的规则校验逻辑
 */
public interface RulesEngine {
    /**
     * 执行规则校验
     *
     * @param rule      规则
     * @param facts     规则校验的事实数据
     * @param arguments 规则校验扩展参数
     * @return 是否通过规则校验
     */
    boolean fire(Rule rule, Facts facts, Object... arguments) throws Exception;

    /**
     * 执行规则校验
     *
     * @param rules     规则集
     * @param facts     规则校验的事实数据
     * @param arguments 规则校验扩展参数
     * @return 是否通过规则校验
     */
    boolean fire(Rules rules, Facts facts, Object... arguments) throws Exception;
}
