package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class IntValue implements Value {
    private int value;

    @Override
    public String toString() {
        return "integer(%d)".formatted(value);
    }
}
