package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record IntConst(int value, int row, int col) implements Number {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
