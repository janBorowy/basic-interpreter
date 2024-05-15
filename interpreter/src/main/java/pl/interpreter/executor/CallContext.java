package pl.interpreter.executor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.EnvironmentException;

public class CallContext {
    private final List<Scope> scopes;
    private final String MISSING_SCOPE_MESSAGE = "Call context does not contain any scope";

    public CallContext(List<Scope> scopes) {
        this.scopes = scopes;
        openNewScope();
    }

    public void openNewScope() {
        scopes.add(getScopeImpl());
    }

    public void openNewScope(Map<String, Value> variablesToRegister) {
        scopes.add(new Scope(variablesToRegister));
    }

    public void closeClosestScope() {
        scopes.removeLast();
    }

    private Scope getClosestScope() {
        return scopes.reversed().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(MISSING_SCOPE_MESSAGE));
    }

    public void setVariableForClosestScope(String id, Value value) {
        scopes.reversed().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(MISSING_SCOPE_MESSAGE))
                .setVariable(id, value);
    }

    public Value resolveVariable(String id) {
        return scopes.reversed().stream()
                .map(s -> s.getVariable(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(() -> new EnvironmentException("Could not resolve variable: \"" + id + "\" in this context"));
    }

    private Scope getScopeImpl() {
        return new Scope(new HashMap<>());
    }
}
