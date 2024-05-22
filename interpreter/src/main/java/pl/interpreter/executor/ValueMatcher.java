package pl.interpreter.executor;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValueMatcher {
    public boolean valueMatchesType(Value value, ValueType type, Environment environment) {
        return switch (value) {
            case IntValue i -> type.getType() == ValueType.Type.INT;
            case FloatValue f -> type.getType() == ValueType.Type.FLOAT;
            case StringValue s -> type.getType() == ValueType.Type.STRING;
            case BooleanValue b -> type.getType() == ValueType.Type.BOOLEAN;
            case StructureValue s -> structureValueMatchesType(s, type, environment);
            case VariantValue v -> type.getType() == ValueType.Type.USER_TYPE && v.getVariantId().equals(type.getUserType());
            case Reference r -> valueMatchesType(r.getReferencedValue(), type, environment);
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }

    private boolean structureValueMatchesType(StructureValue value, ValueType type, Environment environment) {
        var variant = environment.getVariant(type.getUserType());
        return variant.map(v -> structureValueIsOfVariant(value, v))
                .orElseGet(() -> type.getType() == ValueType.Type.USER_TYPE && value.getStructureId().equals(type.getUserType()));
    }

    public boolean structureValueIsOfVariant(StructureValue value, Variant variant) {
        return variant.getStructures()
                .contains(value.getStructureId());
    }
}
