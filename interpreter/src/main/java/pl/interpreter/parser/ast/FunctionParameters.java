package pl.interpreter.parser.ast;

import java.util.List;

public record FunctionParameters(List<ParameterSignature> parameterSignatures) implements Node {}
