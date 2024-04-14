package pl.interpreter.parser.ast;

import java.util.List;

public class Program implements Node {
    private final List<FunctionDefinition> functionDefinitions;
    private final List<StructureDefinition> structureDefinitions;
    private final List<VariantDefinition> variantDefinitions;

    public Program(
            List<FunctionDefinition> functionDefinitions,
            List<StructureDefinition> structureDefinitions,
            List<VariantDefinition> variantDefinitions) {
        this.functionDefinitions = functionDefinitions;
        this.structureDefinitions = structureDefinitions;
        this.variantDefinitions = variantDefinitions;
    }
}
