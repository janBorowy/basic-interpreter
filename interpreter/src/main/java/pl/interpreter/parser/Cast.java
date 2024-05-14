package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class Cast extends Statement implements Expression {

    private Expression expression;

    private PrimitiveType toType;

    public Cast(Expression expression, PrimitiveType toType, Position tokenPosition) {
        super(tokenPosition);
        this.expression = expression;
        this.toType = toType;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
