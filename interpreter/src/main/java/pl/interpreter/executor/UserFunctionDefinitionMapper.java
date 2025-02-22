package pl.interpreter.executor;

import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import pl.interpreter.parser.FunctionDefinition;
import pl.interpreter.parser.FunctionReturnType;
import pl.interpreter.parser.Parameter;
import pl.interpreter.parser.ParameterType;

@UtilityClass
public class UserFunctionDefinitionMapper {

    public UserFunction map(FunctionDefinition functionDefinition) {
        functionDefinition.getReturnType();
        return new UserFunction(mapReturnType(functionDefinition.getReturnType()),
                mapParameters(functionDefinition.getParameters()),
                functionDefinition.getBlock());
    }

    private ValueType mapReturnType(FunctionReturnType type) {
        return switch(type.type()) {
            case VOID -> null;
            case INT -> new ValueType(ValueType.Type.INT);
            case FLOAT -> new ValueType(ValueType.Type.FLOAT);
            case STRING -> new ValueType(ValueType.Type.STRING);
            case BOOL -> new ValueType(ValueType.Type.BOOLEAN);
            case USER_TYPE -> new ValueType(ValueType.Type.USER_TYPE, type.userType());
        };
    }

    private List<FunctionParameter> mapParameters(List<Parameter> astParameters) {
        return astParameters.stream().map((param) -> new FunctionParameter(param.getId(), mapParameterType(param.getType()))).toList();
    }

    private ValueType mapParameterType(ParameterType parameterType) {
        return switch (parameterType.variableType()) {
            case INT -> new ValueType(ValueType.Type.INT);
            case FLOAT -> new ValueType(ValueType.Type.FLOAT);
            case STRING -> new ValueType(ValueType.Type.STRING);
            case BOOL -> new ValueType(ValueType.Type.BOOLEAN);
            case USER_TYPE -> new ValueType(ValueType.Type.USER_TYPE, parameterType.userType());
        };
    }
}
