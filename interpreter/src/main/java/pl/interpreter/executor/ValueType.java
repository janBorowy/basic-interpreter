package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public class ValueType {
    public enum Type {
        INT,
        FLOAT,
        STRING,
        BOOLEAN,
        USER_TYPE
    }

    public ValueType(Type type) {
        this.type = type;
        this.userType = null;
    }

    public boolean isTypeOf(Value value) {
        return switch (value) {
            case IntValue i -> type == Type.INT;
            case FloatValue f -> type == Type.FLOAT;
            case StringValue s -> type == Type.STRING;
            case BooleanValue b -> type == Type.BOOLEAN;
            default -> throw new IllegalStateException("Unexpected implementation: " + value);
        };
    }

    @Override
    public String toString() {
        return switch (type) {
            case INT -> "int";
            case FLOAT -> "float";
            case STRING -> "string";
            case BOOLEAN -> "boolean";
            case USER_TYPE -> userType;
        };
    }

    private Type type;
    private String userType;
}
