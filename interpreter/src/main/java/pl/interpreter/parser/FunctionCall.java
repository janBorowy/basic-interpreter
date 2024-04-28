package pl.interpreter.parser;

import java.util.List;
import lombok.Getter;

@Getter
public class FunctionCall extends Statement implements Expression, Instruction {

    private final String functionId;
    private final List<Expression> arguments;

    public FunctionCall(String functionId, List<Expression> arguments, Position position) {
        super(position);
        this.functionId = functionId;
        this.arguments = arguments;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
