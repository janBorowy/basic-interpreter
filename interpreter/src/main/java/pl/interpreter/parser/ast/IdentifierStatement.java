package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record IdentifierStatement(String identifier, Node statement, int row, int col) implements SingleStatement {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
