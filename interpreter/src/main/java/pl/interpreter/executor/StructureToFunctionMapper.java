package pl.interpreter.executor;

import java.util.List;
import lombok.experimental.UtilityClass;
import pl.interpreter.parser.Parameter;
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
                .map(ASTUtils::valueTypeFromParameterType)
                .toList();
    }
}
