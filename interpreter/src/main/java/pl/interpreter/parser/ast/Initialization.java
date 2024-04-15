package pl.interpreter.parser.ast;

public record Initialization(InitializationSignature initializationSignature, Assignment assignment) implements Node {}
