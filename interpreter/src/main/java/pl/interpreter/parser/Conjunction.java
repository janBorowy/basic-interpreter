package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class Conjunction extends Statement implements Value {

    private final Value left;

    private final Value right;

    public Conjunction(Value left, Value right, Position tokenPosition) {
        super(tokenPosition);
        this.left = left;
        this.right = right;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }
}
