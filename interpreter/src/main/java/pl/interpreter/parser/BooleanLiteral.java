package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class BooleanLiteral extends Statement implements Expression {

    private final boolean on;

    public BooleanLiteral(boolean on, Position tokenPosition) {
        super(tokenPosition);
        this.on = on;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
