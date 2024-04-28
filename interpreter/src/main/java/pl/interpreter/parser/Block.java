package pl.interpreter.parser;

import java.util.List;
import lombok.Getter;

@Getter
public class Block extends Statement implements Instruction {

    private List<Instruction> instructions;

    public Block(List<Instruction> instructions, Position tokenPosition) {
        super(tokenPosition);
        this.instructions = instructions;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
