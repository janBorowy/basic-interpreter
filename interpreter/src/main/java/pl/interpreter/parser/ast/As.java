package pl.interpreter.parser.ast;

public record As(VariableType toType) implements Value, IdentifierValueApplier{}
