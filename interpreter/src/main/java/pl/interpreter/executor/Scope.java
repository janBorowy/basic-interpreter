package pl.interpreter.executor;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.EnvironmentException;

@AllArgsConstructor
public class Scope {

    private final Map<String, Value> variables;

    public Optional<Value> getVariable(String id) {
        var value = variables.get(id);
        if (Objects.nonNull(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public Value setVariable(String id, Value value) {
        if (variables.containsKey(id)) {
            checkIfVariableTypeIsTheSame(id, value);
        }
        variables.put(id, value);
        return variables.get(id);
    }

    public void checkIfVariableTypeIsTheSame(String id, Value value) {
        if (!value.getClass().isInstance(variables.get(id))) {
            throw new EnvironmentException("Changing variable type is forbidden");
        }
    }
}
