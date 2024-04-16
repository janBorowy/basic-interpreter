package pl.interpreter.parser.ast;

import java.util.Optional;

public record ParameterSignature(VariableType type, String identifier, Optional<String> userType) implements Node {
    public ParameterSignature(VariableType type, String identifier) {
        this(type, identifier, Optional.empty());
    }
}
