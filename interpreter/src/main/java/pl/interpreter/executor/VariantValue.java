package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class VariantValue extends Value {
    private String variantId;
    private StructureValue structureValue;

    public VariantValue(String variantId, StructureValue structureValue) {
        this.variantId = variantId;
        this.structureValue = structureValue;
    }

    @Override
    public String toString() {
        return variantId + "(" + structureValue + ")";
    }

    @Override
    public Value clone() {
        return new VariantValue(variantId, (StructureValue) structureValue.clone());
    }
}
