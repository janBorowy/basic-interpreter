package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record BoolConst(boolean value) implements Node {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
