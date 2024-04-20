package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record AfterIdentifierStatement() implements SingleStatement {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
