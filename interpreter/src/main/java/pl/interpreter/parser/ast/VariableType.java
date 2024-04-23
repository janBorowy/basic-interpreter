package pl.interpreter.parser.ast;

import pl.interpreter.TokenType;

public enum VariableType {
    STRING("string"),
    INT("int"),
    FLOAT("float"),
    BOOL("boolean"),
    USER_TYPE("userType");

    private final String string;

    VariableType(String name) {
        this.string = name;
    }

    public static VariableType fromTokenType(TokenType type) {
        return switch (type) {
            case KW_STRING -> STRING;
            case KW_INT -> INT;
            case KW_FLOAT -> FLOAT;
            default -> BOOL;
        };
    }

    @Override
    public String toString() {
        return string;
    }
}
