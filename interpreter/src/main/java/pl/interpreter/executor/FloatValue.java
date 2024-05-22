package pl.interpreter.executor;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode(callSuper = false)
@Setter
public class FloatValue extends Value {
    private float value;
    public FloatValue(float value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "float(%s)".formatted(BigDecimal.valueOf(value).stripTrailingZeros().toPlainString());
    }

    @Override
    public Value clone() {
        return new FloatValue(this.value);
    }
}
