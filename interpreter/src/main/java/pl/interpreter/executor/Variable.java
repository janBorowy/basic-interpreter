package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Variable {
    private final Value value;
    private final boolean mutable;
}
