package pl.interpreter.parser.ast;

public record IdentifierStatement(String identifier, Node statement) implements SingleStatement {}
