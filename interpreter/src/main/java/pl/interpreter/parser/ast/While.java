package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record While(Parentheses parentheses, Instruction instruction, int row, int col) implements CompoundStatement {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
