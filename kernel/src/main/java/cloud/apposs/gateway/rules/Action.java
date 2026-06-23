package cloud.apposs.gateway.rules;

/**
 * 规则条件执行接口规范，用于定义规则条件的基本操作
 */
public interface Action {
    /**
     * 执行规则条件
     */
    void execute(Facts facts, Object... arguments) throws Exception;
}
