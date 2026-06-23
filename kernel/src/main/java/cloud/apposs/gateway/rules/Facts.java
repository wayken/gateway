package cloud.apposs.gateway.rules;

import java.util.*;

/**
 * 事实集合，使用hashset进行存储
 */
public class Facts {
    private final Set<Fact<?>> facts = new HashSet<>();

    /**
     * 获取指定事实数据
     */
    public Fact<?> getFact(String factName) {
        Objects.requireNonNull(factName, "fact name must not be null");
        return facts.stream()
                .filter(fact -> fact.getName().equals(factName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取指定事实数据
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String factName) {
        Objects.requireNonNull(factName, "fact name must not be null");
        Fact<?> fact = getFact(factName);
        if (fact != null) {
            return (T) fact.getValue();
        }
        return null;
    }

    /**
     * 获取指定事实数据，支持多级获取，如：user.name
     */
    public String getValue(String factName) {
        String[] keys = factName.split("\\.");
        if (keys.length == 0) {
            return null;
        }
        Object value = null;
        for (int i = 0; i < keys.length; i++) {
            if (i == 0) {
                value = get(keys[i]);
            } else {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(keys[i]);
                } else {
                    return null;
                }
            }
        }
        return value == null ? null : value.toString();
    }

    /**
     * 获取所有事实数据的映射拷贝
     */
    public Map<String, Object> mapping() {
        Map<String, Object> map = new HashMap<>();
        for (Fact<?> fact : facts) {
            map.put(fact.getName(), fact.getValue());
        }
        return map;
    }

    /**
     * 添加事实数据，如果已经存在对应数据则先删除再添加
     *
     * @param name  事实名称，不允许为null
     * @param value 事实数据，不允许为null
     */
    public <T> void put(String name, T value) {
        Objects.requireNonNull(name, "fact name must not be null");
        Fact<?> retrievedFact = getFact(name);
        if (retrievedFact != null) {
            remove(retrievedFact);
        }
        add(new Fact<>(name, value));
    }

    /**
     * 添加事实数据，如果已经存在对应数据则先删除再添加
     *
     * @param fact 事实数据，不允许为null
     */
    public <T> void add(Fact<T> fact) {
        Objects.requireNonNull(fact, "fact must not be null");
        Fact<?> retrievedFact = getFact(fact.getName());
        if (retrievedFact != null) {
            remove(retrievedFact);
        }
        facts.add(fact);
    }

    /**
     * 移除指定事实数据
     *
     * @param fact 事实数据，不允许为null
     */
    public <T> void remove(Fact<T> fact) {
        Objects.requireNonNull(fact, "fact must not be null");
        facts.remove(fact);
    }
}
