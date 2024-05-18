package pl.interpreter.parser;

import java.util.Optional;
import pl.interpreter.lexical_analyzer.Token;

public enum RelationalOperator {
    EQUALS("=="),
    NOT_EQUALS("!="),
    LESS_THAN("<"),
    GREATER_THAN(">"),
    LESS_THAN_OR_EQUALS("<="),
    GREATER_THAN_OR_EQUALS(">=");

    private final String str;

    RelationalOperator(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }

    public static Optional<RelationalOperator> parse(Token token) {
        return switch (token.type()) {
            case EQUALS_OPERATOR -> Optional.of(EQUALS);
            case NOT_EQUALS_OPERATOR -> Optional.of(NOT_EQUALS);
            case LESS_THAN_OPERATOR -> Optional.of(LESS_THAN);
            case GREATER_THAN_OPERATOR -> Optional.of(GREATER_THAN);
            case LESS_THAN_OR_EQUALS_OPERATOR -> Optional.of(LESS_THAN_OR_EQUALS);
            case GREATER_THAN_OR_EQUALS_OPERATOR -> Optional.of(GREATER_THAN_OR_EQUALS);
            default -> Optional.empty();
        };
    }
}
