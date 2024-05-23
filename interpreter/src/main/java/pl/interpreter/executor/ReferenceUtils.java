package pl.interpreter.executor;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ReferenceUtils {

    public Value getReferencedValue(Value value) {
        if (value instanceof Reference ref) {
            value = ref.getReferencedValue();
        }
        return value;
    }

    public Value getReferencedValue(Variable variable) {
        return getReferencedValue(variable.getValue());
    }
}
