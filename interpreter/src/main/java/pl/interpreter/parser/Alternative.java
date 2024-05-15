package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class Alternative extends Statement implements Expression {

    private Expression left;
    private Expression right;

    public Alternative(Expression left, Expression right, Position tokenPosition) {
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
