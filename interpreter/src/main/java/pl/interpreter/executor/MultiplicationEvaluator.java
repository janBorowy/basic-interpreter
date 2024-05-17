package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.ExpressionEvaluationException;
import pl.interpreter.executor.exceptions.ValueTypeException;

@AllArgsConstructor
public class MultiplicationEvaluator {

    public enum Operator {
        MULTIPLICATION,
        DIVISION,
        MODULO
    }

    private final Value leftHandSide;
    private final Value rightHandSide;
    private final Operator operator;

    public Value evaluate() {
        return switch (leftHandSide) {
            case IntValue i -> doMultiplication(i);
            case FloatValue f -> doMultiplication(f);
            default -> throw new ValueTypeException("Multiplication operations are only allowed for integer and float types");
        };
    }

    private Value doMultiplication(IntValue lhs) {
        return switch (operator) {
            case MULTIPLICATION -> doMultiply(lhs);
            case DIVISION-> doDivide(lhs);
            case MODULO -> doModulo(lhs);
        };
    }

    private Value doMultiplication(FloatValue lhs) {
        return switch (operator) {
            case MULTIPLICATION -> doMultiply(lhs);
            case DIVISION -> doDivide(lhs);
            case MODULO -> doModulo(lhs);
        };
    }

    private Value doMultiply(IntValue lhs) {
        return switch (rightHandSide) {
            case IntValue rhs -> new IntValue(lhs.getValue() * rhs.getValue());
            case FloatValue rhs -> new FloatValue(lhs.getValue() * rhs.getValue());
            default -> throw new IllegalStateException("Unexpected value: " + rightHandSide);
        };
    }

    private Value doMultiply(FloatValue lhs) {
        return switch (rightHandSide) {
            case IntValue rhs -> new FloatValue(lhs.getValue() * rhs.getValue());
            case FloatValue rhs -> new FloatValue(lhs.getValue() * rhs.getValue());
            default -> throw new IllegalStateException("Unexpected value: " + rightHandSide);
        };
    }

    private Value doDivide(IntValue lhs) {
        validateNotZero(rightHandSide);
        return switch (rightHandSide) {
            case IntValue rhs -> new IntValue(lhs.getValue() / rhs.getValue());
            case FloatValue rhs -> new FloatValue(lhs.getValue() / rhs.getValue());
            default -> throw new IllegalStateException("Unexpected value: " + rightHandSide);
        };
    }

    private Value doDivide(FloatValue lhs) {
        validateNotZero(rightHandSide);
        return switch (rightHandSide) {
            case IntValue rhs -> new FloatValue(lhs.getValue() / rhs.getValue());
            case FloatValue rhs -> new FloatValue(lhs.getValue() / rhs.getValue());
            default -> throw new IllegalStateException("Unexpected value: " + rightHandSide);
        };
    }

    private Value doModulo(IntValue lhs) {
        validateNotZero(rightHandSide);
        return switch (rightHandSide) {
            case IntValue rhs -> new IntValue(lhs.getValue() % rhs.getValue());
            case FloatValue rhs -> new FloatValue(lhs.getValue() % rhs.getValue());
            default -> throw new IllegalStateException("Unexpected value: " + rightHandSide);
        };
    }

    private Value doModulo(FloatValue lhs) {
        validateNotZero(rightHandSide);
        return switch (rightHandSide) {
            case IntValue rhs -> new FloatValue(lhs.getValue() % rhs.getValue());
            case FloatValue rhs -> new FloatValue(lhs.getValue() % rhs.getValue());
            default -> throw new IllegalStateException("Unexpected value: " + rightHandSide);
        };
    }

    private void validateNotZero(Value rhs) {
        switch (rhs) {
            case IntValue i -> {
                if (i.getValue() == 0) {
                    throw new ExpressionEvaluationException("Division by zero is forbidden");
                }
            }
            case FloatValue f ->{
                if (Float.compare(f.getValue(), 0.0f) == 0) {
                    throw new ExpressionEvaluationException("Division by zero is forbidden");
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + rhs);
        }
    }
}
