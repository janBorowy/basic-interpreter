package pl.interpreter.executor;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.InitializationException;

@AllArgsConstructor
public class Scope {

    private final Map<String, Variable> variables;

    public Optional<Variable> getVariable(String id) {
        var variable = variables.get(id);
        if (Objects.nonNull(variable)) {
            return Optional.of(variable);
        }
        return Optional.empty();
    }

    public Variable initializeVariable(String id, Variable variable) {
        if (variables.containsKey(id)) {
            throw new InitializationException("%s was already initialized".formatted(id));
        }
        variables.put(id, variable);
        return variables.get(id);
    }
}
