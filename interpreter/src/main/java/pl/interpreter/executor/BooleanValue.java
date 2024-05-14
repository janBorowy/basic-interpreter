package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BooleanValue implements Value {
    private boolean isTruthy;
}
