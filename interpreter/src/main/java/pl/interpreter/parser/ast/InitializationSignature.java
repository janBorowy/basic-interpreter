package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record InitializationSignature(boolean isVar, Node type, String identifier) implements Node {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
