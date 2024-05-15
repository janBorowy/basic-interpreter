package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class Multiplication extends Statement implements Expression {

    private Expression left;
    private MultiplicationOperator operator;
    private Expression right;

    public Multiplication(Expression left, MultiplicationOperator operator, Expression right, Position tokenPosition) {
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
