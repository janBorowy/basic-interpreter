package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class Conjunction extends Statement implements Expression {

    private final Expression left;

    private final Expression right;

    public Conjunction(Expression left, Expression right, Position tokenPosition) {
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
