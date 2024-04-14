package pl.interpreter.parser.ast;

public record Initialization(InitializationSignature initializationSignature, Value value) implements Node {}
