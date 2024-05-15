package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class WhileStatement extends Statement implements Instruction{

    private final Value expression;
    private final Instruction instruction;

    public WhileStatement(Value expression, Instruction instruction, Position tokenPosition) {
        super(tokenPosition);
        this.expression = expression;
        this.instruction = instruction;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
