package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class Multiplication extends Statement implements Value {

    private Value left;
    private MultiplicationOperator operator;
    private Value right;

    public Multiplication(Value left, MultiplicationOperator operator, Value right, Position tokenPosition) {
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
