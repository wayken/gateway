package cloud.apposs.gateway.rules.mvel;

import cloud.apposs.gateway.rules.Condition;
import cloud.apposs.gateway.rules.Facts;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

import java.io.Serializable;

public class MVELCondition implements Condition {
    private final String expression;
    private final Serializable compiledExpression;

    public MVELCondition(String expression) {
        this.expression = expression;
        this.compiledExpression = MVEL.compileExpression(expression);
    }

    public MVELCondition(String expression, ParserContext parserContext) {
        this.expression = expression;
        this.compiledExpression = MVEL.compileExpression(expression, parserContext);
    }

    @Override
    public boolean evaluate(Facts facts, Object... arguments) {
        if (expression == null || expression.isEmpty()) {
            return true;
        }
        return (boolean) MVEL.executeExpression(compiledExpression, facts.mapping());
    }

    public Serializable getExpression() {
        return compiledExpression;
    }

    @Override
    public int hashCode() {
        return compiledExpression.hashCode();
    }

    @Override
    public String toString() {
        return "MVELCondition {" +
                "expression=" + compiledExpression +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MVELCondition instance = (MVELCondition) obj;
        return compiledExpression.equals(instance.compiledExpression);
    }
}
