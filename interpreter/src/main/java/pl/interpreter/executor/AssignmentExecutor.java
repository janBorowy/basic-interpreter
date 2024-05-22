package pl.interpreter.executor;

import java.sql.Ref;
import pl.interpreter.executor.exceptions.AssignmentException;

public class AssignmentExecutor {
    private final String variableId;
    private final Value valueToAssign;
    private final Environment environment;
    private ValueType expectedType;

    public AssignmentExecutor(String variableId, Value valueToAssign, Environment environment) {
        this.variableId = variableId;
        this.valueToAssign = ReferenceUtils.getReferencedValue(valueToAssign);
        this.environment = environment;
    }

    public void assign() {
        var variable = environment.getCurrentContext().resolveVariable(variableId);
        var variableValue = variable.getValue();
        expectedType = TypeUtils.getTypeOf(variableValue);
        validate(variable);
        if (variableValue instanceof Reference ref) {
            assignForReference(ref, valueToAssign);
            return;
        }
        if (TypeUtils.isVariant(expectedType, environment)) {
            assignForVariant(variable);
            return;
        }
        variable.setValue(valueToAssign);
    }

    private void assignForReference(Reference reference, Value valueToAssign) {
        switch (reference.getReferencedValue()) {
            case IntValue i -> i.setValue(((IntValue)valueToAssign).getValue());
            case FloatValue f -> f.setValue(((FloatValue)valueToAssign).getValue());
            case StringValue s -> s.setValue(((StringValue)valueToAssign).getValue());
            case BooleanValue b -> b.setTruthy(((BooleanValue)valueToAssign).isTruthy());
            case StructureValue s -> s.setFields(((StructureValue) valueToAssign).getFields());
            case VariantValue v -> {
                if (valueToAssign instanceof VariantValue var) {
                    v.setStructureValue((var.getStructureValue()));
                } else {
                    v.setStructureValue((StructureValue) valueToAssign);
                }
            }
            default -> throw new IllegalStateException("Unimplemented value definition: " + valueToAssign);
        }
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
            throw new AssignmentException("%s is not mutable".formatted(variable));
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
