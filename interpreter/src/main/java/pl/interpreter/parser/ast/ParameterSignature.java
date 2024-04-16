package pl.interpreter.parser.ast;

public record ParameterSignature(VariableType type, String identifier) implements Node {}
