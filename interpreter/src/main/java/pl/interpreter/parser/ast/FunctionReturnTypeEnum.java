package pl.interpreter.parser.ast;

import pl.interpreter.TokenType;

public enum FunctionReturnTypeEnum {
    STRING("string"),
    INT("int"),
    FLOAT("float"),
    BOOL("boolean"),
    VOID("void"),
    USER_TYPE("userType");

    private final String string;

    FunctionReturnTypeEnum(String name) {
        string = name;
    }

    public static FunctionReturnTypeEnum getFromTokenType(TokenType type) {
        return switch(type) {
            case KW_INT -> INT;
            case KW_STRING -> STRING;
            case KW_FLOAT -> FLOAT;
            default -> BOOL;
        };
    }

    @Override
    public String toString() {
        return string;
    }
}
