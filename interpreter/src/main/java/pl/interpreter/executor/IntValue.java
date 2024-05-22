package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode(callSuper = false)
@Setter
public class IntValue extends Value {
    private int value;

    public IntValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "integer(%d)".formatted(value);
    }

    @Override
    public Value clone() {
        return new IntValue(this.value);
    }
}
