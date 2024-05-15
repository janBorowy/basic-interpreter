package pl.interpreter.executor;

import java.util.List;

public interface Function {
    Value execute(List<Value> arguments);
    ValueType getReturnType();
}
