package pl.interpreter.parser.ast;

import java.util.List;
import pl.interpreter.parser.NodeVisitor;

public record Subcondition(List<BooleanExpression> expressions, int row, int col) implements Node {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
