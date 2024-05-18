package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class StringValue implements Value {
    private String value;

    @Override
    public String toString() {
        return "string(%s)".formatted(value);
    }
}
