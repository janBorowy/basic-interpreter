package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.InvalidValueTypeException;

@AllArgsConstructor
public class NegationEvaluator {

    private final Value operand;

    public Value evaluate() {
        return switch (operand) {
            case BooleanValue b -> new BooleanValue(!(b.isTruthy()));
            default -> throw new InvalidValueTypeException("Expected boolean");
        };
    }
}
