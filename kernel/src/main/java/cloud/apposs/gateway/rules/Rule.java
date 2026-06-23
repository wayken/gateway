package cloud.apposs.gateway.rules;

/**
 * 规则接口规范，用于定义规则的基本操作
 */
public interface Rule extends Comparable<Rule> {
    String DEFAULT_NAME = "rule";

    /**
     * 默认规则描述
     */
    String DEFAULT_DESCRIPTION = "description";

    /**
     * 默认规则优先级
     */
    int DEFAULT_PRIORITY = Integer.MAX_VALUE - 1;

    /**
     * 规则的名称,在同一个规则引擎下名字必须唯一
     */
    default String getName() {
        return DEFAULT_NAME;
    }

    /**
     * 规则的描述
     */
    default String getDescription() {
        return DEFAULT_DESCRIPTION;
    }

    /**
     * 规则的优先级，值越大优先级越高，默认Integer最大值减一
     */
    default int getPriority() {
        return DEFAULT_PRIORITY;
    }

    /**
     * 是否在第一个匹配的规则时就跳过后续规则的执行，默认为true
     */
    default boolean isSkipOnFirstAppliedRule() {
        return true;
    }

    /**
     * 规则的条件判断，达到什么条件时执行 execute
     *
     * @param  facts     事实数据
     * @param  arguments 规则校验扩展参数
     * @return 是否满足条件，满足条件则执行 execute
     */
    boolean evaluate(Facts facts, Object... arguments);

    /**
     * 符合规则时需要执行的方法
     *
     * @param facts     事实数据
     * @param arguments 规则校验扩展参数
     */
    void execute(Facts facts, Object... arguments) throws Exception;
}
