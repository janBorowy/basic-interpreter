package pl.interpreter.parser.ast;

public record UserTypeInitialization(String typeIdentifier, String identifier, Value valueAssigned) implements Initialization {}
