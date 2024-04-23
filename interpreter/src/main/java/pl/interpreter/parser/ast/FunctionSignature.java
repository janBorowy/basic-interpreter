package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record FunctionSignature(FunctionReturnType returnType, String identifier, int row, int col) implements Node {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
