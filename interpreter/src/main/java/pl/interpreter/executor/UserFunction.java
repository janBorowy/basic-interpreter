package pl.interpreter.executor;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import pl.interpreter.parser.Block;

@AllArgsConstructor
public class UserFunction implements Function {

    private ValueType returnType;
    private Map<String, ValueType> parameters;
    private Block block;

    @Override
    public Value execute(List<Value> arguments) {
        return null;
    }

    @Override
    public ValueType getReturnType() {
        return null;
    }
}
