package pl.interpreter.parser.ast;

import java.util.List;
import pl.interpreter.parser.NodeVisitor;

public record VariantDefinition(String identifier, List<String> variantIdentifiers, int row, int col) implements Definition {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
