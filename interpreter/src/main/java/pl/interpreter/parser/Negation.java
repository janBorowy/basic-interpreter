package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class Negation extends Statement implements Value {

    private final Value expression;

    public Negation(Value expression, Position tokenPosition) {
        super(tokenPosition);
        this.expression = expression;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }
}
