package pl.interpreter.executor.built_in_functions;

import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.interpreter.executor.Environment;
import pl.interpreter.executor.Function;
import pl.interpreter.executor.Value;
import pl.interpreter.executor.ValueType;

@AllArgsConstructor
public abstract class BuiltInFunction implements Function {

    @Getter
    private ValueType returnType;

    @Getter
    private List<ValueType> expectedParameterTypes;

    @Getter
    protected Environment environment;

    public abstract Value execute(List<Value> arguments) throws IOException;
}
