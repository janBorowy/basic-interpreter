package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record VarInitialization(Initialization initialization, int row, int col) implements SingleStatement {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
