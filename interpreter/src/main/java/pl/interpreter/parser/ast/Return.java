package pl.interpreter.parser.ast;

import java.util.Optional;
import pl.interpreter.parser.NodeVisitor;

public record Return(Optional<Value> value, int row, int col) implements SingleStatement {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
