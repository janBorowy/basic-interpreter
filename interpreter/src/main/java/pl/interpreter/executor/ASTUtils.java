package pl.interpreter.executor;

import lombok.experimental.UtilityClass;
import pl.interpreter.parser.ParameterType;
import pl.interpreter.parser.VariableType;

@UtilityClass
public class ASTUtils {

    public ValueType valueTypeFromParameterType(ParameterType parameterType) {
        return valueTypeFromVariableType(parameterType.variableType(), parameterType.userType());
    }

    public ValueType valueTypeFromVariableType(VariableType variableType, String userType) {
        return switch (variableType) {
            case INT -> new ValueType(ValueType.Type.INT);
            case FLOAT -> new ValueType(ValueType.Type.FLOAT);
            case STRING -> new ValueType(ValueType.Type.STRING);
            case BOOL -> new ValueType(ValueType.Type.BOOLEAN);
            case USER_TYPE -> new ValueType(ValueType.Type.USER_TYPE, userType);
        };
    }
}
