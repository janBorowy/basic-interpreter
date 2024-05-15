package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class Cast extends Statement implements Value {

    private Value expression;

    private PrimitiveType toType;

    public Cast(Value expression, PrimitiveType toType, Position tokenPosition) {
        super(tokenPosition);
        this.expression = expression;
        this.toType = toType;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }
}
