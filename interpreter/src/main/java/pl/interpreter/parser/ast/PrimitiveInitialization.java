package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record PrimitiveInitialization(VariableType type, String identifier, Value valueAssigned, int row, int col) implements Initialization {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
