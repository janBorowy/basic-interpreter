package pl.interpreter.parser.ast;

public record VariableAssignment(String identifier, Value valueAssigned) implements Node {}
