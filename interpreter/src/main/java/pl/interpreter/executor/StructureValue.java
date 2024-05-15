package pl.interpreter.executor;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StructureValue implements Value {
    private final String structureName;
    private final Map<String, Value> fields;
}
