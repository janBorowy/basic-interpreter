package pl.interpreter.parser.ast;

import java.util.List;
import pl.interpreter.parser.NodeVisitor;

public record Block(List<Instruction> statements, int row, int col) implements Instruction {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
