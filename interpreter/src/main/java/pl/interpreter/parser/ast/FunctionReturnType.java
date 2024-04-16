package pl.interpreter.parser.ast;

import java.util.Optional;

public record FunctionReturnType(FunctionReturnTypeEnum type, Optional<String> userType) {
    public FunctionReturnType(FunctionReturnTypeEnum type) {
        this(type, Optional.empty());
    }
}
