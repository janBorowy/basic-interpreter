package pl.interpreter.executor;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.AssignmentException;
import pl.interpreter.executor.exceptions.EnvironmentException;
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

    public Variable setVariable(String id, Value value) {
        var variable = getVariable(id)
                .orElseThrow(() -> new AssignmentException("Variable was not initialized"));
        checkIfVariableIsMutable(id, variable);
        checkIfVariableIsOfTheSameType(id, value, variable);
        variables.put(id, new Variable(value, true));
        return variables.get(id);
    }

    public Variable initializeVariable(String id, Variable variable) {
        if (variables.containsKey(id)) {
            throw new InitializationException("%s was already initialized".formatted(id));
        }
        variables.put(id, variable);
        return variables.get(id);
    }

    private void checkIfVariableIsMutable(String id, Variable variable) {
        if (!variable.isMutable()) {
            throw new AssignmentException(id + " is not mutable");
        }
    }

    private void checkIfVariableIsOfTheSameType(String id, Value value, Variable variable) {
        var givenType = TypeUtils.getTypeOf(value);
        var expectedType = TypeUtils.getTypeOf(variable.getValue());
        if (!givenType.equals(expectedType)) {
            throw new AssignmentException("Cannot change type of %s from %s to %s".formatted(id, expectedType, givenType));
        }
    }
}
