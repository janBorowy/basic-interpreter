package pl.interpreter.parser.ast;

import pl.interpreter.TokenType;

public enum VariableTypeEnum {
    STRING,
    INT,
    FLOAT,
    BOOL;

    public static VariableTypeEnum tokenTypeToVariableType(TokenType type) {
        return switch (type) {
            case KW_STRING -> STRING;
            case KW_INT -> INT;
            case KW_FLOAT -> FLOAT;
            default -> BOOL;
        };
    }
}
