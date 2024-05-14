package pl.interpreter.executor;

import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CallContext {
    private final List<Scope> scopes;
}
