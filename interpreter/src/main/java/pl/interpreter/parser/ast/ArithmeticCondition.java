package pl.interpreter.parser.ast;

import java.util.Optional;
import pl.interpreter.TokenType;

public enum ArithmeticCondition {
    EQUAL,
    NOT_EQUAL,
    LESS_THAN,
    GREATER_THAN,
    LESS_THAN_OR_EQUAL,
    GREATER_THAN_OR_EQUAL;

    public static Optional<ArithmeticCondition> fromTokenType(TokenType type) {
        return switch (type) {
            case EQUALS_OPERATOR -> Optional.of(EQUAL);
            case NOT_EQUALS_OPERATOR -> Optional.of(NOT_EQUAL);
            case LESS_THAN_OPERATOR -> Optional.of(LESS_THAN);
            case GREATER_THAN_OPERATOR -> Optional.of(GREATER_THAN);
            case LESS_THAN_OR_EQUALS_OPERATOR -> Optional.of(LESS_THAN_OR_EQUAL);
            case GREATER_THAN_OR_EQUALS_OPERATOR -> Optional.of(GREATER_THAN_OR_EQUAL);
            default -> Optional.empty();
        };
    }
}
