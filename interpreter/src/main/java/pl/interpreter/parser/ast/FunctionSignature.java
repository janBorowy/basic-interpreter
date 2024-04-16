package pl.interpreter.parser.ast;

public record FunctionSignature(FunctionReturnType returnType, String identifier) implements Node {}
