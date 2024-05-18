package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.InitializationException;

@AllArgsConstructor
public class InitializationExecutor {

    private final String variableId;
    private final ValueType type;
    private final Value valueToAssign;
    private final boolean isVar;
    private final Environment environment;

    public void execute() {
        validateType();
        if (TypeUtils.isVariant(type, environment)) {
            assignForVariant();
            return;
        }
        environment.getCurrentContext().initializeVariableForClosestScope(variableId, new Variable(valueToAssign, isVar));
    }

    private void validateType() {
        if (!ValueMatcher.valueMatchesType(valueToAssign, type, environment)) {
            throw new InitializationException("Value(%s) does not match variable type(%s)".formatted(valueToAssign, type));
        }
    }

    private void assignForVariant() {
        if (TypeUtils.isVariant(TypeUtils.getTypeOf(valueToAssign), environment)) {
            environment.getCurrentContext().initializeVariableForClosestScope(variableId, new Variable(valueToAssign, isVar));
        } else {
            environment.getCurrentContext().initializeVariableForClosestScope(variableId,
                    new Variable(new VariantValue(type.getUserType(), (StructureValue) valueToAssign), isVar));
        }
    }
}
