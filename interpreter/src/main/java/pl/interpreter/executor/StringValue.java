package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode(callSuper = false)
@Setter
public class StringValue extends Value {
    private String value;
    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "string(%s)".formatted(value);
    }

    @Override
    public Value clone() {
        return new StringValue(this.value);
    }
}
