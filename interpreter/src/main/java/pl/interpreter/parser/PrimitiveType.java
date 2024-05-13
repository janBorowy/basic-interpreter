package pl.interpreter.parser;

import java.util.Optional;
import pl.interpreter.Token;

public enum PrimitiveType {
    INT("int"),
    FLOAT("float"),
    STRING("string"),
    BOOL("boolean");

    private final String str;

    PrimitiveType(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }

    public static Optional<PrimitiveType> parse(Token token) {
        return switch (token.type()) {
            case KW_INT -> Optional.of(INT);
            case KW_FLOAT -> Optional.of(FLOAT);
            case KW_STRING -> Optional.of(STRING);
            case KW_BOOL -> Optional.of(BOOL);
            default -> Optional.empty();
        };
    }
}
