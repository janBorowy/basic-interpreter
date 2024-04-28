package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class Assignment extends Statement implements Instruction {

    private final String id;
    private final Expression expression;

    public Assignment(String id, Expression expression, Position tokenPosition) {
        super(tokenPosition);
        this.id = id;
        this.expression = expression;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
