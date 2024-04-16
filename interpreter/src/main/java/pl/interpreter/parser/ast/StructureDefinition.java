package pl.interpreter.parser.ast;

import java.util.List;

public record StructureDefinition(String identifier, List<ParameterSignature> parameterSignatures) implements Definition {}
