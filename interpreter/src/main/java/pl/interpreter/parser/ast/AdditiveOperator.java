package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record AdditiveOperator(Operator operator) implements Node {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
