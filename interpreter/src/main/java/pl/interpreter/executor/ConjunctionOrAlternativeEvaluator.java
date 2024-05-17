package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.ValueTypeException;

@AllArgsConstructor
public class ConjunctionOrAlternativeEvaluator {

    enum Operator {
        CONJUNCTION,
        ALTERNATIVE
    }

    private final Value leftHandSide;
    private final Value rightHandSide;
    private final Operator operator;

    public Value evaluate() {
        return switch (leftHandSide) {
            case BooleanValue lhs -> validateRight(lhs);
            default -> throw new ValueTypeException("Expected boolean");
        };
    }

    public static boolean shouldShortCircuit(Value leftHandSide, Operator operator) {
        return switch (leftHandSide) {
            case BooleanValue lhs -> shouldShortCircuitOn(lhs.isTruthy(), operator);
            default -> throw new ValueTypeException("Expected boolean");
        };
    }

    private static boolean shouldShortCircuitOn(boolean value, Operator operator) {
        return switch (operator) {
            case CONJUNCTION -> !value;
            case ALTERNATIVE -> value;
        };
    }

    private Value validateRight(BooleanValue lhs) {
        return switch (rightHandSide) {
            case BooleanValue rhs -> doConjunction(lhs, rhs);
            default -> throw new ValueTypeException("Expected boolean");
        };
    }

    private Value doConjunction(BooleanValue lhs, BooleanValue rhs) {
        return switch (operator) {
            case CONJUNCTION -> new BooleanValue(lhs.isTruthy() && rhs.isTruthy());
            case ALTERNATIVE -> new BooleanValue(lhs.isTruthy() || rhs.isTruthy());
        };
    }

}
