package pl.interpreter.parser.ast;

import java.util.List;
import pl.interpreter.parser.NodeVisitor;

public record FunctionCall(List<Value> values, int row, int col) implements IdentifierStatementApplier, IdentifierValueApplier {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
