package pl.interpreter.executor;

import java.util.List;
import lombok.experimental.UtilityClass;
import pl.interpreter.parser.Parameter;
import pl.interpreter.parser.ParameterType;
import pl.interpreter.parser.StructureDefinition;

@UtilityClass
public class StructureToFunctionMapper {

    public Function map(StructureDefinition definition) {
        return new StructureConstructor(definition.getId(), getFieldNames(definition), getParameterTypes(definition));
    }

    private List<String> getFieldNames(StructureDefinition definition) {
        return definition.getParameters().stream()
                .map(Parameter::getId)
                .toList();
    }

    private List<ValueType> getParameterTypes(StructureDefinition definition) {
        return definition.getParameters().stream()
                .map(Parameter::getType)
                .map(StructureToFunctionMapper::getValueTypeFromParameterType)
                .toList();
    }

    private ValueType getValueTypeFromParameterType(ParameterType type) {
        var variableType = switch (type.variableType()) {
            case INT -> ValueType.Type.INT;
            case FLOAT -> ValueType.Type.FLOAT;
            case STRING -> ValueType.Type.STRING;
            case BOOL -> ValueType.Type.BOOLEAN;
            case USER_TYPE -> ValueType.Type.USER_TYPE;
        };
        return new ValueType(variableType, type.userType());
    }
}
