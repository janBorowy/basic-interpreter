package pl.interpreter.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class StructureValue extends Value {

    private final String structureId;
    private Map<String, Value> fields;

    public StructureValue(String structureId, Map<String, Value> fields) {
        this.structureId = structureId;
        this.fields = fields;
    }

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

    @Override
    public Value clone() {
        return new StructureValue(structureId, new HashMap<>(fields));
    }
}
