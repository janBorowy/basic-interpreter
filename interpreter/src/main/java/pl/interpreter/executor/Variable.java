package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class Variable {
    @Setter
    private Value value;
    private final boolean mutable;
}
