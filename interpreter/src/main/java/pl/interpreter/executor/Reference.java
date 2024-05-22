package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Reference extends Value {
    private Value referencedValue;
    private boolean mutable;

    @Override
    public Value clone() {
        return new Reference(referencedValue, mutable);
    }

    @Override
    public String toString() {
        return "reference(%s)".formatted(referencedValue);
    }
}
