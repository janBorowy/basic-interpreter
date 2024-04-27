package pl.interpreter.parser;

import java.util.Optional;
import pl.interpreter.Token;

public enum AdditionOperator {
    PLUS("+"),
    MINUS("-");

    private final String str;

    AdditionOperator(String str) {
        this.str = str;
    }

    public static Optional<AdditionOperator> parseAdditionOperator(Token token) {
        return switch(token.type()) {
            case ADD_OPERATOR -> Optional.of(PLUS);
            case SUBTRACT_OPERATOR -> Optional.of(MINUS);
            default -> Optional.empty();
        };
    }

    @Override
    public String toString() {
        return str;
    }
}
