package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.ValueTypeException;

public class NegationEvaluator {

    private final Value operand;

    public NegationEvaluator(Value operand) {
        this.operand = ReferenceUtils.getReferencedValue(operand);
    }

    public Value evaluate() {
        return switch (operand) {
            case BooleanValue b -> new BooleanValue(!(b.isTruthy()));
            default -> throw new ValueTypeException("Expected boolean");
        };
    }
}
