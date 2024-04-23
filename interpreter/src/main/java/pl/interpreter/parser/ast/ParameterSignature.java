package pl.interpreter.parser.ast;

import java.util.Optional;
import pl.interpreter.parser.NodeVisitor;

public record ParameterSignature(VariableType type, String identifier, Optional<String> userType, int row, int col) implements Node {
    public ParameterSignature(VariableType type, String identifier, int row, int col) {
        this(type, identifier, Optional.empty(), row, col);
    }
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
