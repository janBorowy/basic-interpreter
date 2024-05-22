package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.ExpressionEvaluationException;

public class RelationEvaluator {

    public enum Operator {
        EQUAL,
        NOT_EQUAL,
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN,
        LESS_THAN_OR_EQUAL
    }

    public RelationEvaluator(Value leftHandSide, Value rightHandSide, Operator operator) {
        this.leftHandSide = ReferenceUtils.getReferencedValue(leftHandSide);
        this.rightHandSide = ReferenceUtils.getReferencedValue(rightHandSide);
        this.operator = operator;
    }

    private final Value leftHandSide;
    private final Value rightHandSide;
    private final Operator operator;

    public Value evaluate() {
        return switch (leftHandSide) {
            case IntValue i -> doValidateRhs(i);
            case FloatValue f -> doValidateRhs(f);
            default -> throw new ExpressionEvaluationException("Relation operations are only allowed for integer and float type");
        };
    }

    private Value doValidateRhs(IntValue lhs) {
        return switch (rightHandSide) {
            case IntValue i -> doCompare(lhs, i);
            case FloatValue f -> doCompare(lhs, f);
            default -> throw new IllegalStateException("Unexpected value: " + rightHandSide);
        };
    }

    private Value doValidateRhs(FloatValue lhs) {
        return switch (rightHandSide) {
            case IntValue i -> doCompare(lhs, i);
            case FloatValue f -> doCompare(lhs, f);
            default -> throw new IllegalStateException("Unexpected value: " + rightHandSide);
        };
    }

    private Value doCompare(IntValue lhs, IntValue rhs) {
        return switch (operator) {
            case EQUAL -> new BooleanValue(lhs.getValue() == rhs.getValue());
            case NOT_EQUAL -> new BooleanValue(lhs.getValue() != rhs.getValue());
            case GREATER_THAN -> new BooleanValue(lhs.getValue() > rhs.getValue());
            case GREATER_THAN_OR_EQUAL -> new BooleanValue(lhs.getValue() >= rhs.getValue());
            case LESS_THAN -> new BooleanValue(lhs.getValue() < rhs.getValue());
            case LESS_THAN_OR_EQUAL -> new BooleanValue(lhs.getValue() <= rhs.getValue());
        };
    }

    private Value doCompare(FloatValue lhs, FloatValue rhs) {
        return switch (operator) {
            case EQUAL -> new BooleanValue(lhs.getValue() == rhs.getValue());
            case NOT_EQUAL -> new BooleanValue(lhs.getValue() != rhs.getValue());
            case GREATER_THAN -> new BooleanValue(lhs.getValue() > rhs.getValue());
            case GREATER_THAN_OR_EQUAL -> new BooleanValue(lhs.getValue() >= rhs.getValue());
            case LESS_THAN -> new BooleanValue(lhs.getValue() < rhs.getValue());
            case LESS_THAN_OR_EQUAL -> new BooleanValue(lhs.getValue() <= rhs.getValue());
        };
    }

    private Value doCompare(IntValue lhs, FloatValue rhs) {
        return switch (operator) {
            case EQUAL -> new BooleanValue(lhs.getValue() == rhs.getValue());
            case NOT_EQUAL -> new BooleanValue(lhs.getValue() != rhs.getValue());
            case GREATER_THAN -> new BooleanValue(lhs.getValue() > rhs.getValue());
            case GREATER_THAN_OR_EQUAL -> new BooleanValue(lhs.getValue() >= rhs.getValue());
            case LESS_THAN -> new BooleanValue(lhs.getValue() < rhs.getValue());
            case LESS_THAN_OR_EQUAL -> new BooleanValue(lhs.getValue() <= rhs.getValue());
        };
    }

    private Value doCompare(FloatValue lhs, IntValue rhs) {
        return switch (operator) {
            case EQUAL -> new BooleanValue(lhs.getValue() == rhs.getValue());
            case NOT_EQUAL -> new BooleanValue(lhs.getValue() != rhs.getValue());
            case GREATER_THAN -> new BooleanValue(lhs.getValue() > rhs.getValue());
            case GREATER_THAN_OR_EQUAL -> new BooleanValue(lhs.getValue() >= rhs.getValue());
            case LESS_THAN -> new BooleanValue(lhs.getValue() < rhs.getValue());
            case LESS_THAN_OR_EQUAL -> new BooleanValue(lhs.getValue() <= rhs.getValue());
        };
    }
}
