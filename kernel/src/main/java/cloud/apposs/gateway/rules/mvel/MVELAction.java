package cloud.apposs.gateway.rules.mvel;

import cloud.apposs.gateway.rules.Action;
import cloud.apposs.gateway.rules.Facts;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

import java.io.Serializable;

public class MVELAction implements Action {
    private final String expression;
    private final Serializable compiledExpression;

    public MVELAction(String expression) {
        this.expression = expression;
        this.compiledExpression = MVEL.compileExpression(expression);
    }

    public MVELAction(String expression, ParserContext parserContext) {
        this.expression = expression;
        this.compiledExpression = MVEL.compileExpression(expression, parserContext);
    }

    @Override
    public void execute(Facts facts, Object... arguments) throws Exception {
        MVEL.executeExpression(compiledExpression, facts.mapping());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MVELAction instance = (MVELAction) obj;
        return expression.equals(instance.expression);
    }
}
