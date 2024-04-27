package pl.interpreter.parser;

import lombok.Getter;

public class ReturnStatement extends Statement {

    @Getter
    private Expression expression;

    public ReturnStatement(Expression expression, Position tokenPosition) {
        super(tokenPosition);
        this.expression = null;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
