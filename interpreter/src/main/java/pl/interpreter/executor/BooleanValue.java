package pl.interpreter.executor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class BooleanValue extends Value {
    private boolean isTruthy;

    public BooleanValue(boolean isTruthy) {
        this.isTruthy = isTruthy;
    }

    @Override
    public String toString() {
        return "boolean(%b)".formatted(isTruthy);
    }

    @Override
    public Value clone() {
        return new BooleanValue(this.isTruthy);
    }
}
