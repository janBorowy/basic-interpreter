package pl.interpreter.parser.ast;

public record FunctionSignature(Node returnType, String identifier) implements Node {}
