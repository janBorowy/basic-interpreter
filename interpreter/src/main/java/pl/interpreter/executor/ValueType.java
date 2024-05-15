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

    public boolean typeOf(Value value) {
        return switch (value) {
            case IntValue i -> type == Type.INT;
            case FloatValue f -> type == Type.FLOAT;
            case StringValue s -> type == Type.STRING;
            case BooleanValue b -> type == Type.BOOLEAN;
            default -> throw new IllegalStateException("Unexpected implementation: " + value);
        };
    }

    private Type type;
    private String userType;
}
