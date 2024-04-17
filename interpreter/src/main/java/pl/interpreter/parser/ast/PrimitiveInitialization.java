package pl.interpreter.parser.ast;

public record PrimitiveInitialization(VariableType type, String identifier, Value valueAssigned) implements Initialization {}
