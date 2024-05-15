package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class DotAccess extends Statement implements Value {

    private final Value expression;
    private final String fieldName;

    public DotAccess(Value expression, String fieldName, Position tokenPosition) {
        super(tokenPosition);
        this.expression = expression;
        this.fieldName = fieldName;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }
}
