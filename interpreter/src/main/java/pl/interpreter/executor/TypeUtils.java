package pl.interpreter.executor;

import lombok.experimental.UtilityClass;
import pl.interpreter.executor.exceptions.EnvironmentException;

@UtilityClass
public class TypeUtils {

    public ValueType getTypeOf(Value value) {
        return switch (value) {
            case IntValue i -> new ValueType(ValueType.Type.INT);
            case StringValue s -> new ValueType(ValueType.Type.STRING);
            case FloatValue f -> new ValueType(ValueType.Type.FLOAT);
            case BooleanValue b -> new ValueType(ValueType.Type.BOOLEAN);
            case StructureValue sv -> new ValueType(ValueType.Type.USER_TYPE, sv.getStructureId());
            case VariantValue vv -> new ValueType(ValueType.Type.USER_TYPE, vv.getVariantId());
            case Reference r -> getTypeOf(r.getReferencedValue());
            case null, default -> null;
        };
    }

    public void validateUserTypeExists(ValueType type, Environment environment) {
        if (type != null && type.getType() == ValueType.Type.USER_TYPE) {
            validateUserTypeExists(type.getUserType(), environment);
        }
    }

    public void validateUserTypeExists(String type, Environment environment) {
        if (!(environment.getFunction(type).isPresent() || environment.getVariant(type).isPresent())) {
            throw new EnvironmentException("User type \"%s\" does not exist".formatted(type));
        }
    }

    public boolean isVariant(ValueType type, Environment environment) {
        if (type.getType() != ValueType.Type.USER_TYPE) {
            return false;
        }
        return environment.getVariant(type.getUserType()).isPresent();
    }
}
