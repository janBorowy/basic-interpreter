package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record As(VariableType toType) implements Value, IdentifierValueApplier {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
