package cloud.apposs.gateway.rules;

/**
 * 规则条件接口规范，用于定义规则条件的基本操作
 */
public interface Condition {
    /**
     * 规则条件的判断
     */
    boolean evaluate(Facts facts, Object... arguments);

    Condition FALSE = (facts, arguments) -> false;

    Condition TRUE = (facts, arguments) -> true;
}
