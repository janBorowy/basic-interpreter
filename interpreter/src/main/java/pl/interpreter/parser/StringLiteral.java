package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class StringLiteral extends Statement implements Expression {

    private String value;

    public StringLiteral(String value, Position tokenPosition) {
        super(tokenPosition);
        this.value = value;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
