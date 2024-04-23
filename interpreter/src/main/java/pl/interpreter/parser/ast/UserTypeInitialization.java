package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record UserTypeInitialization(String typeIdentifier, String identifier, Value valueAssigned, int row, int col) implements Initialization {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
