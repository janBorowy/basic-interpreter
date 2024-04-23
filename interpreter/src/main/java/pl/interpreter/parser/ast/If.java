package pl.interpreter.parser.ast;

import java.util.Optional;
import pl.interpreter.parser.NodeVisitor;

public record If(Parentheses parentheses, Instruction instruction, Optional<Instruction> elseInstruction, int row, int col) implements CompoundStatement {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
