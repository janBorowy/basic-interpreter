package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record VariableAssignment(String identifier, Value valueAssigned, int row, int col) implements Node {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
