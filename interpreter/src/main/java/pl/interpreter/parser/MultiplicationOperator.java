package pl.interpreter.parser;

import java.util.Optional;
import pl.interpreter.lexical_analyzer.Token;

public enum MultiplicationOperator {
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("%");

    private final String str;

    MultiplicationOperator(String str) {
        this.str = str;
    }

    public static Optional<MultiplicationOperator> parse(Token token) {
        return switch(token.type()) {
            case MULTIPLY_OPERATOR -> Optional.of(MULTIPLY);
            case DIVIDE_OPERATOR -> Optional.of(DIVIDE);
            case MODULO_OPERATOR -> Optional.of(MODULO);
            default -> Optional.empty();
        };
    }

    @Override
    public String toString() {
        return str;
    }
}