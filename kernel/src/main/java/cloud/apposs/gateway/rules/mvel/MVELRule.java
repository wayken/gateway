package cloud.apposs.gateway.rules.mvel;

import cloud.apposs.gateway.rules.*;
import org.mvel2.ParserContext;

import java.util.ArrayList;
import java.util.List;

public class MVELRule extends BasicRule {
    private Condition condition = Condition.FALSE;

    private final List<Action> actions = new ArrayList<Action>();

    private final ParserContext parserContext;

    public MVELRule(String name) {
        this(new ParserContext());
        this.name = name;
    }

    public MVELRule(ParserContext parserContext) {
        super(Rule.DEFAULT_NAME, Rule.DEFAULT_DESCRIPTION, Rule.DEFAULT_PRIORITY);
        this.parserContext = parserContext;
    }

    public MVELRule name(String name) {
        this.name = name;
        return this;
    }

    public MVELRule description(String description) {
        this.description = description;
        return this;
    }

    public MVELRule priority(int priority) {
        this.priority = priority;
        return this;
    }

    public MVELRule when(String condition) {
        this.condition = new MVELCondition(condition, parserContext);
        return this;
    }

    public MVELRule then(String action) {
        this.actions.add(new MVELAction(action, parserContext));
        return this;
    }

    public MVELRule then(Action action) {
        this.actions.add(action);
        return this;
    }

    @Override
    public boolean evaluate(Facts facts, Object... arguments) {
        return condition.evaluate(facts, arguments);
    }

    @Override
    public void execute(Facts facts, Object... arguments) throws Exception {
        for (Action action : actions) {
            action.execute(facts, arguments);
        }
    }

    public Condition getCondition() {
        return condition;
    }
}
