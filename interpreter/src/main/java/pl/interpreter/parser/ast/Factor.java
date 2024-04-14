package pl.interpreter.parser.ast;

// if string - identifier
public record Factor(Object value) implements Node {}
