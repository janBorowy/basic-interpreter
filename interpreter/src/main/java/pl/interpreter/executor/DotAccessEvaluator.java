package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.AccessException;
import pl.interpreter.executor.exceptions.ValueTypeException;

@AllArgsConstructor
public class DotAccessEvaluator {

    private Value leftHandSide;
    private String fieldName;

    public Value evaluate() {
        return switch (leftHandSide) {
            case StructureValue struct -> doDotAccess(struct);
            default -> throw new ValueTypeException("Can only access fields of structures");
        };
    }

    private Value doDotAccess(StructureValue struct) {
        return struct.getField(fieldName)
                .orElseThrow(() -> new AccessException("Structure does not have a field: " + fieldName));
    }
}
