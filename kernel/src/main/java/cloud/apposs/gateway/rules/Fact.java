package cloud.apposs.gateway.rules;

import java.util.Objects;

/**
 * 规则引擎的事实数据，用于规则的条件判断
 */
public class Fact<T> {
    private final String name;

    private final T value;

    public Fact(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fact<?> fact = (Fact<?>) o;
        return name.equals(fact.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Fact{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
