package pl.interpreter.parser.ast;

import java.util.Optional;
import pl.interpreter.parser.NodeVisitor;

public record FunctionReturnType(FunctionReturnTypeEnum type, Optional<String> userType, int row, int col) implements Node {
    public FunctionReturnType(FunctionReturnTypeEnum type, int row, int col) {
        this(type, Optional.empty(), row, col);
    }
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
