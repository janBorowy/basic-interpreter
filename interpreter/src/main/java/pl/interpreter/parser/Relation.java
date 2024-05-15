package pl.interpreter.parser;

import lombok.Getter;

public class Relation extends Statement implements Value {

    @Getter
    private final Value left;
    @Getter
    private final Value right;
    @Getter
    private final RelationalOperator operator;

    public Relation(Value left, RelationalOperator operator, Value right, Position position) {
        super(position);
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
