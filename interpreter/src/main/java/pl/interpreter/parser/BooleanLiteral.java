package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class BooleanLiteral extends Statement implements Expression {

    private final boolean truthy;

    public BooleanLiteral(boolean on, Position tokenPosition) {
        super(tokenPosition);
        this.truthy = on;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }
}
