package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record UserType(String identifier) implements Node {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
