package pl.interpreter.executor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.interpreter.executor.exceptions.InvalidFunctionCallException;
import pl.interpreter.executor.exceptions.InvalidParameterTypeException;

@Getter
@AllArgsConstructor
public class StructureConstructor implements Function {

    private final String structureName;
    private final List<String> fieldNames;
    private final List<ValueType> expectedParameterTypes;

    @Override
    public Value execute(List<Value> arguments) {
        validate(arguments);
        return new StructureValue(structureName, getFields(arguments));
    }

    @Override
    public ValueType getReturnType() {
        return new ValueType(ValueType.Type.USER_TYPE, structureName);
    }

    private void validate(List<Value> arguments) {
        if (arguments.size() != expectedParameterTypes.size()) {
            throw new InvalidFunctionCallException("Does not match function parameters");
        }
        if (!argumentTypesMatch(arguments)) {
            throw new InvalidParameterTypeException("Invalid parameter type");
        }
    }

    private boolean argumentTypesMatch(List<Value> arguments) {
        return IntStream.range(0, arguments.size())
                .allMatch(i -> expectedParameterTypes.get(i).typeOf(arguments.get(i)));
    }

    private Map<String, Value> getFields(List<Value> arguments) {
        var fields = new HashMap<String, Value>();
        IntStream.range(0, fieldNames.size())
                .forEach(i -> fields.put(fieldNames.get(i), arguments.get(i)));
        return fields;
    }
}
