package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class Negation extends Statement implements Expression {

    private final Expression expression;

    public Negation(Expression expression, Position tokenPosition) {
        super(tokenPosition);
        this.expression = expression;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
