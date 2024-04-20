package pl.interpreter.parser.ast;

public record BooleanExpression(LogicTerm logicTerm, boolean negated) implements Node {}
