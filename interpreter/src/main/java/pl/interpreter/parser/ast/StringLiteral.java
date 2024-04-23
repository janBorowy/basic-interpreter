package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record StringLiteral(String value, int row, int col) implements InplaceValue {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
