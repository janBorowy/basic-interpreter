package pl.interpreter.executor;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StructureConstructor implements Function {

    private final String structureName;
    private final List<String> fieldNames;
    private final List<ValueType> expectedParameterTypes;

    @Override
    public ValueType getReturnType() {
        return new ValueType(ValueType.Type.USER_TYPE, structureName);
    }
}
