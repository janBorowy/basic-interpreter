package pl.interpreter.executor;

import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StructureValue implements Value {
    private final String structureName;
    private final Map<String, Value> fields;

    public Optional<Value> getField(String id) {
        return Optional.ofNullable(fields.get(id));
    }
}
