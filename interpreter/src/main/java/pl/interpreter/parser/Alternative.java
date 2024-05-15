package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class Alternative extends Statement implements Value {

    private Value left;
    private Value right;

    public Alternative(Value left, Value right, Position tokenPosition) {
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
