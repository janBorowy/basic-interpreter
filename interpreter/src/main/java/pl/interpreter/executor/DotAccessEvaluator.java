package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.InvalidAccessException;
import pl.interpreter.executor.exceptions.InvalidValueTypeException;

@AllArgsConstructor
public class DotAccessEvaluator {

    private Value leftHandSide;
    private String fieldName;

    public Value evaluate() {
        return switch (leftHandSide) {
            case StructureValue struct -> doDotAccess(struct);
            default -> throw new InvalidValueTypeException("Can only access fields of structures");
        };
    }

    private Value doDotAccess(StructureValue struct) {
        return struct.getField(fieldName)
                .orElseThrow(() -> new InvalidAccessException("Structure does not have a field: " + fieldName));
    }
}
