package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class VariantValue implements Value {
    private String variantId;
    private StructureValue structureValue;
}
