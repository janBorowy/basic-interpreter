package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class MatchBranch extends Statement {

    private final String structureId;
    private final String fieldName;
    private final Instruction instruction;

    public MatchBranch(String structureId, String fieldName, Instruction instruction, Position tokenPosition) {
        super(tokenPosition);
        this.structureId = structureId;
        this.fieldName = fieldName;
        this.instruction = instruction;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
