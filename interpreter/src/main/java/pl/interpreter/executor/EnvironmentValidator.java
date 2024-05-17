package pl.interpreter.executor;

import java.util.Map;
import lombok.AllArgsConstructor;
import pl.interpreter.executor.exceptions.VariantException;

@AllArgsConstructor
public class EnvironmentValidator {

    private final Environment environment;

    public void validateVariants(Map<String, Variant> variants) {
        variants.values().forEach(this::validateSingleVariant);
    }

    public void validateFunctions(Map<String, Function> functions) {
        functions.values()
                .stream()
                .filter(UserFunction.class::isInstance)
                .map(UserFunction.class::cast)
                .forEach(this::validateSingleFunction);
    }

    public void validateStructures(Map<String, Function> structures) {
        structures.values()
                .stream()
                .filter(StructureConstructor.class::isInstance)
                .map(StructureConstructor.class::cast)
                .forEach(this::validateSingleStructure);
    }

    private void validateSingleStructure(StructureConstructor structureConstructor) {
        structureConstructor.getExpectedParameterTypes()
                .forEach(it -> TypeUtils.validateUserTypeExists(it, environment));
    }

    private void validateSingleVariant(Variant variant) {
        variant.getStructures()
                .forEach(this::validateStructureDefinitionExists);
    }

    private void validateSingleFunction(UserFunction function) {
        TypeUtils.validateUserTypeExists(function.getReturnType(), environment);
        function.getParameters().forEach(it -> TypeUtils.validateUserTypeExists(it.valueType(), environment));
    }

    private void validateStructureDefinitionExists(String structureName) {
        var function = environment.getFunction(structureName)
                .orElseThrow(() -> new VariantException("Structure definition \"%s\" does not exist".formatted(structureName)));
        if (!(function instanceof StructureConstructor)) {
            throw new VariantException("Structure definition \"%s\" does not exist".formatted(structureName));
        }
    }
}
