package pl.interpreter.parser;

import java.util.List;
import lombok.Getter;

@Getter
public class FunctionCall extends Statement implements Value, Instruction {

    private final String functionId;
    private final List<Value> arguments;

    public FunctionCall(String functionId, List<Value> arguments, Position position) {
        super(position);
        this.functionId = functionId;
        this.arguments = arguments;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }
}
