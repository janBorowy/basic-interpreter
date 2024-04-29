package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class WhileStatement extends Statement implements Instruction{

    private final Expression expression;
    private final Instruction instruction;

    public WhileStatement(Expression expression, Instruction instruction, Position tokenPosition) {
        super(tokenPosition);
        this.expression = expression;
        this.instruction = instruction;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
