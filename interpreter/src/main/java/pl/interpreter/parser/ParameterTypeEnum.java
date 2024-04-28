package pl.interpreter.parser;

import java.util.Optional;
import pl.interpreter.Token;

public enum ParameterTypeEnum {
    INT("int"),
    FLOAT("float"),
    STRING("string"),
    BOOL("boolean"),
    USER_TYPE("user_type");

    private final String str;

    ParameterTypeEnum(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }

    public static Optional<ParameterTypeEnum> parseParameterType(Token token) {
        return switch (token.type()) {
            case KW_INT -> Optional.of(INT);
            case KW_FLOAT -> Optional.of(FLOAT);
            case KW_STRING -> Optional.of(STRING);
            case KW_BOOL -> Optional.of(BOOL);
            case IDENTIFIER -> Optional.of(USER_TYPE);
            default -> Optional.empty();
        };
    }
}
