package pl.interpreter.parser.ast;

public record InitializationSignature(boolean isVar, Node type, String identifier) implements Node {}
