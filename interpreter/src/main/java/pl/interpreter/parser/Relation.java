package pl.interpreter.parser;

import lombok.Getter;

public class Relation extends Statement implements Expression {

    @Getter
    private final Expression left;
    @Getter
    private final Expression right;
    @Getter
    private final RelationalOperator operator;

    public Relation(Expression left, RelationalOperator operator, Expression right, Position position) {
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
