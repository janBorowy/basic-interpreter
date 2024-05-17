package pl.interpreter.executor.built_in_functions;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import pl.interpreter.executor.Environment;

@UtilityClass
public class BuiltInFunctionRegistry {

    public static Map<String, BuiltInFunction> prepareBuiltInFunctionsForEnvironment(Environment environment) {
        Map<String, BuiltInFunction> map = new HashMap<>();
        map.put("print", new PrintFunction(environment));
        return map;
    }
}
