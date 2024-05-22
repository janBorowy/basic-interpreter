package pl.interpreter.executor.built_in_functions;

import java.io.IOException;
import java.util.List;
import pl.interpreter.executor.Environment;
import pl.interpreter.executor.ReferenceUtils;
import pl.interpreter.executor.StringValue;
import pl.interpreter.executor.Value;
import pl.interpreter.executor.ValueType;

public class PrintLineFunction extends BuiltInFunction {

    public PrintLineFunction(Environment environment) {
        super(null, List.of(new ValueType(ValueType.Type.STRING)), environment);
    }

    @Override
    public Value execute(List<Value> arguments) throws IOException {
        var value = ReferenceUtils.getReferencedValue(arguments.getFirst());
        switch (value) {
            case StringValue s -> environment.getStandardOutput().write(s.getValue() + '\n');
            default -> throw new IllegalStateException("Argument should have been validated");
        }
        return null;
    }
}
