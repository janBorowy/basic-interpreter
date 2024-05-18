package pl.interpreter.executor;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class FloatValue implements Value {
    private float value;

    @Override
    public String toString() {
        return "float(%s)".formatted(BigDecimal.valueOf(value).stripTrailingZeros().toPlainString());
    }
}
