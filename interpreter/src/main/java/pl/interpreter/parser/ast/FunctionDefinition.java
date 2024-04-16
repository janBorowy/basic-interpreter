package pl.interpreter.parser.ast;

import java.util.List;

public record FunctionDefinition(FunctionSignature functionSignature, List<ParameterSignature> functionParameters, Block block) implements Definition {}
