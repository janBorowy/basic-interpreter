package pl.interpreter.executor;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TypeUtils {

    public ValueType getTypeOf(Value value) {
        return switch (value) {
            case IntValue i -> new ValueType(ValueType.Type.INT);
            case StringValue s -> new ValueType(ValueType.Type.STRING);
            case FloatValue f -> new ValueType(ValueType.Type.FLOAT);
            case BooleanValue b -> new ValueType(ValueType.Type.BOOLEAN);
            case StructureValue sv -> new ValueType(ValueType.Type.USER_TYPE, sv.getStructureName());
            case null -> null;
            default -> throw new IllegalStateException("Unknown implementation: " + value);
        };
    }
}
