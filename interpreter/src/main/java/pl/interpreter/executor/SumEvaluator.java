package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.ExpressionEvaluationException;
import pl.interpreter.executor.exceptions.InvalidValueTypeException;

@AllArgsConstructor
public class SumEvaluator {
    public enum SumOperator {
        PLUS,
        MINUS
    }
    private Value leftHandSide;
    private Value rightHandSide;
    private SumOperator operator;

    public Value evaluate() {
        validate();
        return switch (leftHandSide) {
            case StringValue s -> doConcat(s);
            case IntValue i -> doSum(i);
            case FloatValue f -> doSum(f);
            default -> throw new ExpressionEvaluationException();
        };
    }

    private void validate() {
        switch(leftHandSide) {
            case BooleanValue b -> throw new InvalidValueTypeException("Boolean values do not support sum operation");
            case StringValue s -> {
                if (operator.equals(SumOperator.MINUS)) {
                    throw new ExpressionEvaluationException("Expected \"+\" operator");
                }
                if (!(rightHandSide instanceof StringValue)) {
                    throw new InvalidValueTypeException("Right hand-side must be string");
                }
            }
            case IntValue i -> {
                if (rightHandSide instanceof StringValue || rightHandSide instanceof BooleanValue) {
                    throw new InvalidValueTypeException("Only int and float sum operations are allowed");
                }
            }
            case FloatValue f -> {
                if (rightHandSide instanceof StringValue || rightHandSide instanceof BooleanValue) {
                    throw new InvalidValueTypeException("Only int and float sum operations are allowed");
                }
            }
            default -> {}
        }
    }

    private StringValue doConcat(StringValue lhs) {
        return new StringValue(lhs.getValue() + ((StringValue) rightHandSide).getValue());
    }

    private Value doSum(IntValue lhs) {
        return switch (operator) {
            case PLUS -> doAdd(lhs);
            case MINUS -> doSub(lhs);
        };
    }

    private Value doSum(FloatValue lhs) {
        return switch (operator) {
            case PLUS -> doAdd(lhs);
            case MINUS -> doSub(lhs);
        };
    }

    private Value doAdd(IntValue lhs) {
        return switch (rightHandSide) {
            case IntValue i -> new IntValue(lhs.getValue() + i.getValue());
            case FloatValue f -> new FloatValue(lhs.getValue() + f.getValue());
            default -> throw new ExpressionEvaluationException();
        };
    }

    private Value doAdd(FloatValue lhs) {
        return switch (rightHandSide) {
            case IntValue i -> new FloatValue(lhs.getValue() + i.getValue());
            case FloatValue f -> new FloatValue(lhs.getValue() + f.getValue());
            default -> throw new ExpressionEvaluationException();
        };
    }

    private Value doSub(IntValue lhs) {
        return switch (rightHandSide) {
            case IntValue i -> new IntValue(lhs.getValue() - i.getValue());
            case FloatValue f -> new FloatValue(lhs.getValue() - f.getValue());
            default -> throw new ExpressionEvaluationException();
        };
    }

    private Value doSub(FloatValue lhs) {
        return switch (rightHandSide) {
            case IntValue i -> new FloatValue(lhs.getValue() - i.getValue());
            case FloatValue f -> new FloatValue(lhs.getValue() - f.getValue());
            default -> throw new ExpressionEvaluationException();
        };
    }
}
