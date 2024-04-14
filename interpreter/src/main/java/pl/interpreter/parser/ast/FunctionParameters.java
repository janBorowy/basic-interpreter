package pl.interpreter.parser.ast;

import java.util.List;
import java.util.Objects;

public record FunctionParameters(List<ParameterSignature> parameterSignatures) implements Node {}
