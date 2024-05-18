package pl.interpreter.executor;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import pl.interpreter.executor.exceptions.EnvironmentException;

public class CallContext {
    private final List<Scope> scopes;
    private static final String MISSING_SCOPE_MESSAGE = "Call context does not contain any scope";

    public CallContext(List<Scope> scopes) {
        this.scopes = scopes;
    }

    public void openNewScope() {
        scopes.add(getScopeImpl());
    }

    public void closeClosestScope() {
        scopes.removeLast();
    }

    public void initializeVariableForClosestScope(String id, Variable variable) {
        scopes.reversed().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(MISSING_SCOPE_MESSAGE))
                .initializeVariable(id, variable);
    }

    public Variable resolveVariable(String id) {
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
