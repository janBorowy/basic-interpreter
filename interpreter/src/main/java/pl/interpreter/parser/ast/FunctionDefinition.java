package pl.interpreter.parser.ast;

public record FunctionDefinition(FunctionSignature functionSignature, FunctionParameters functionParameters, Block block) implements Node {}
