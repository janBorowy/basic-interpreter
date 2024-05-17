package pl.interpreter.executor;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.ValueTypeException;

@AllArgsConstructor
public class CastEvaluator {

    enum LegalCastType {
        INT,
        FLOAT,
        STRING,
        BOOLEAN
    }

    private final Value leftHandSide;
    private final LegalCastType toType;

    public Value evaluate() {
        return switch (toType) {
            case INT -> validateIntCast();
            case FLOAT -> validateFloatCast();
            case STRING -> validateStringCast();
            case BOOLEAN -> throw new ValueTypeException("Can't cast to boolean values");
        };
    }

    private Value validateIntCast() {
        return switch (leftHandSide) {
            case IntValue i -> leftHandSide;
            case FloatValue f -> new IntValue((int) f.getValue());
            default -> throw new ValueTypeException("Conversion to integer is only allowed for integer and float types");
        };
    }

    private Value validateFloatCast() {
        return switch (leftHandSide) {
            case IntValue i -> new FloatValue(i.getValue());
            case FloatValue f -> leftHandSide;
            default -> throw new ValueTypeException("Conversion to integer is only allowed for integer and float types");
        };
    }

    private Value validateStringCast() {
        return switch (leftHandSide) {
            case IntValue i -> new StringValue(String.valueOf(i.getValue()));
            case FloatValue f -> new StringValue(getFormattedFloatString(f.getValue()));
            case StringValue s -> leftHandSide;
            case BooleanValue b -> new StringValue(b.isTruthy() ? "true" : "false");
            default -> throw new IllegalStateException("Unknown value implementation: " + leftHandSide);
        };
    }

    private String getFormattedFloatString(float f) {
        return new BigDecimal(f).stripTrailingZeros().toPlainString();
    }
}
