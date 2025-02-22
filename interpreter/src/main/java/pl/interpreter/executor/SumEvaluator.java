package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.ExpressionEvaluationException;
import pl.interpreter.executor.exceptions.InvalidValueTypeException;

@AllArgsConstructor
public class SumEvaluator {

    private final static String ONLY_INT_FLOAT_ALLOWED_MSG = "Only int and float sum operations are allowed";

    public enum Operator {
        PLUS,
        MINUS
    }
    private final Value leftHandSide;
    private final Value rightHandSide;
    private Operator operator;

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
            case StringValue s -> {
                if (operator.equals(Operator.MINUS)) {
                    throw new ExpressionEvaluationException("Expected \"+\" operator");
                }
                if (!(rightHandSide instanceof StringValue)) {
                    throw new InvalidValueTypeException("Right hand-side must be string");
                }
            }
            case IntValue i -> {
                if (rightHandSide instanceof StringValue || rightHandSide instanceof BooleanValue) {
                    throw new InvalidValueTypeException(ONLY_INT_FLOAT_ALLOWED_MSG);
                }
            }
            case FloatValue f -> {
                if (rightHandSide instanceof StringValue || rightHandSide instanceof BooleanValue) {
                    throw new InvalidValueTypeException(ONLY_INT_FLOAT_ALLOWED_MSG);
                }
            }
            default -> throw new InvalidValueTypeException(ONLY_INT_FLOAT_ALLOWED_MSG);
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
