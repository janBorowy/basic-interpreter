package pl.interpreter.parser.ast;

import pl.interpreter.TokenType;

public enum FunctionReturnTypeEnum {
    STRING,
    INT,
    FLOAT,
    BOOL,
    VOID,
    USER_TYPE;

    public static FunctionReturnTypeEnum getFromTokenType(TokenType type) {
        return switch(type) {
            case KW_INT -> INT;
            case KW_STRING -> STRING;
            case KW_FLOAT -> FLOAT;
            default -> BOOL;
        };
    }
}
