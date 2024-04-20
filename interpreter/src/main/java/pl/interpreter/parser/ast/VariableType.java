package pl.interpreter.parser.ast;

import pl.interpreter.TokenType;

public enum VariableType {
    STRING,
    INT,
    FLOAT,
    BOOL,
    USER_TYPE;

    public static VariableType fromTokenType(TokenType type) {
        return switch (type) {
            case KW_STRING -> STRING;
            case KW_INT -> INT;
            case KW_FLOAT -> FLOAT;
            default -> BOOL;
        };
    }
}
