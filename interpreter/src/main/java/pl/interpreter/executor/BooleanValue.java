package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class BooleanValue implements Value {
    private boolean isTruthy;

    @Override
    public String toString() {
        return "boolean(%b)".formatted(isTruthy);
    }
}
