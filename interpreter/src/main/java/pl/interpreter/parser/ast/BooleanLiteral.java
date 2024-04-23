package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record BooleanLiteral(boolean value, int row, int col) implements LogicTerm {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
