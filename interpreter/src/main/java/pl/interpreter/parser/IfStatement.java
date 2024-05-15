package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class IfStatement extends Statement implements Instruction {

    private final Expression expression;
    private final Instruction instruction;
    private final Instruction elseInstruction;

    public IfStatement(Expression expression, Instruction instruction, Instruction elseInstruction, Position tokenPosition) {
        super(tokenPosition);
        this.expression = expression;
        this.instruction = instruction;
        this.elseInstruction = elseInstruction;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
