package pl.interpreter.parser.ast;

public record ParameterSignature(Node type, String identifier) implements Node {}
