package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class IntLiteral extends Statement implements Expression{

    private final int value;

    public IntLiteral(int value, Position tokenPosition) {
        super(tokenPosition);
        this.value = value;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
