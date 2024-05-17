package pl.interpreter.executor;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StructureValue implements Value {
    private final String structureId;
    private final Map<String, Value> fields;

    public Optional<Value> getField(String id) {
        return Optional.ofNullable(fields.get(id));
    }

    @Override
    public String toString() {
        return structureId + "(" + getFieldsString() + ")";
    }

    private String getFieldsString() {
        return fields.entrySet()
                .stream()
                .map(this::getFieldString)
                .collect(Collectors.joining(", "));
    }

    private String getFieldString(Map.Entry<String, Value> field) {
        return field.getKey() + ": " + field.getValue();
    }

}
