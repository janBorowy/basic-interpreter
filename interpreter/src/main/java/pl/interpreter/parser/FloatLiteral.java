package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class FloatLiteral extends Statement implements Expression {

    private final float value;

    public FloatLiteral(float value, Position tokenPosition) {
        super(tokenPosition);
        this.value = value;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
