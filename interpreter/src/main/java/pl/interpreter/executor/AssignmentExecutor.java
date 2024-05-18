package pl.interpreter.executor;

import lombok.RequiredArgsConstructor;
import pl.interpreter.executor.exceptions.AssignmentException;


@RequiredArgsConstructor
public class AssignmentExecutor {
    private final String variableId;
    private final Value valueToAssign;
    private final Environment environment;
    private ValueType expectedType;

    public void assign() {
        var variable = environment.getCurrentContext().resolveVariable(variableId);
        expectedType = TypeUtils.getTypeOf(variable.getValue());
        validate(variable);
        if (TypeUtils.isVariant(expectedType, environment)) {
            assignForVariant(variable);
            return;
        }
        variable.setValue(valueToAssign);
    }

    private void validate(Variable variable) {
        validateType();
        validateIsMutable(variable);
    }

    private void validateType() {
        if (!ValueMatcher.valueMatchesType(valueToAssign, expectedType, environment)) {
            throw new AssignmentException("Value(%s) does not match variable type(%s)".formatted(valueToAssign, expectedType));
        }
    }

    private void validateIsMutable(Variable variable) {
        if (!variable.isMutable()) {
            throw new AssignmentException("Variable(%s) is not mutable".formatted(variable));
        }
    }

    private void assignForVariant(Variable variable) {
        if (TypeUtils.isVariant(TypeUtils.getTypeOf(valueToAssign), environment)) {
            variable.setValue(valueToAssign);
        } else {
            variable.setValue(new VariantValue(expectedType.getUserType(), (StructureValue) valueToAssign));
        }
    }
}
