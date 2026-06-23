package cloud.apposs.gateway.rules;

import java.util.*;

/**
 * 网关规则集合，用于管理网关的规则集合
 */
public class Rules implements Iterable<Rule> {
    private Set<Rule> rules = new TreeSet<Rule>();

    /**
     * 创建一个新的{@link Rules}对象
     * 规则每次添加在集合内部会自动进行排序
     *
     * @return rules 规则集合
     */
    public Rules(Set<Rule> rules) {
        this.rules = new TreeSet<>(rules);
    }

    /**
     * 创建一个新的{@link Rules}对象
     *
     * @param rules 规则集合
     */
    public Rules(Rule... rules) {
        Collections.addAll(this.rules, rules);
    }

    /**
     * 注册一个或多个规则
     *
     * @param rules 规则集合，不能为空
     */
    public void register(Rule... rules) {
        Objects.requireNonNull(rules);
        for (Rule rule : rules) {
            this.rules.add(rule);
        }
    }

    /**
     * 更新一个或多个规则，如果规则不存在则注册
     */
    public void update(Rule... rules) {
        Objects.requireNonNull(rules);
        for (Rule rule : rules) {
            this.rules.remove(rule);
            this.rules.add(rule);
        }
    }

    /**
     * 注销一个或多个规则
     *
     * @param rules 规则集合，不能为空
     */
    public void unregister(Rule... rules) {
        Objects.requireNonNull(rules);
        for (Rule rule : rules) {
            this.rules.remove(rule);
        }
    }

    /**
     * 通过规则名称注销规则
     *
     * @param ruleName 规则名称，不能为空
     */
    public void unregister(final String ruleName) {
        Objects.requireNonNull(ruleName);
        Rule rule = findRuleByName(ruleName);
        if (rule != null) {
            unregister(rule);
        }
    }

    /**
     * 判断规则集合是否为空
     */
    public boolean isEmpty() {
        return rules.isEmpty();
    }

    /**
     * 清空规则集合
     */
    public void clear() {
        rules.clear();
    }

    /**
     * 获取规则集合大小
     */
    public int size() {
        return rules.size();
    }

    @Override
    public Iterator<Rule> iterator() {
        return rules.iterator();
    }

    private Rule findRuleByName(String ruleName) {
        return rules.stream()
                .filter(rule -> rule.getName().equalsIgnoreCase(ruleName))
                .findFirst()
                .orElse(null);
    }
}
