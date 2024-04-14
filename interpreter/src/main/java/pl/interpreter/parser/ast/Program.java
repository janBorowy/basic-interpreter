package pl.interpreter.parser.ast;

import java.util.List;

public record Program(List<FunctionDefinition> functionDefinitions, List<StructureDefinition> structureDefinitions,
                      List<VariantDefinition> variantDefinitions) implements Node {
}
