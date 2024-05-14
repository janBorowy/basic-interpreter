package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class Sum extends Statement implements Expression {

    private Expression left;
    private AdditionOperator operator;
    private Expression right;

    public Sum(Expression left, AdditionOperator operator, Expression right, Position tokenPosition) {
        super(tokenPosition);
        this.left = left;
        this.operator = operator;
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
