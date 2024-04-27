package pl.interpreter.parser;

import java.util.List;
import lombok.Getter;

@Getter
public class FunctionCall extends Statement implements Expression {

    private final String functionId;
    private final List<Expression> parameters;

    public FunctionCall(String functionId, List<Expression> parameters, Position position) {
        super(position);
        this.functionId = functionId;
        this.parameters = parameters;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
