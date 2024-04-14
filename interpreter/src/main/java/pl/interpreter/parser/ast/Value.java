package pl.interpreter.parser.ast;

/* Value can hold string - identifier, else it is Node */
public record Value(Object valueOrigin) implements Node {}
