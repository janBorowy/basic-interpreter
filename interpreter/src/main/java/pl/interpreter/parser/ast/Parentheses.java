package pl.interpreter.parser.ast;

import java.util.List;
import pl.interpreter.parser.NodeVisitor;

public record Parentheses(List<Subcondition> subconditions, int row, int col) implements ParenthesesValue {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
