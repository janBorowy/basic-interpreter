package pl.interpreter.parser.ast;

import java.util.Optional;
import pl.interpreter.parser.NodeVisitor;

public record IdentifierWithValue(String value, Optional<IdentifierValueApplier> applier, int row, int col) implements Factor {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
