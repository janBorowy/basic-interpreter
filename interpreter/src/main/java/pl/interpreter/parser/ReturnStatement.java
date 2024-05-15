package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class ReturnStatement extends Statement implements Instruction {

    private Expression expression;

    public ReturnStatement(Expression expression, Position tokenPosition) {
        super(tokenPosition);
        this.expression = expression;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
