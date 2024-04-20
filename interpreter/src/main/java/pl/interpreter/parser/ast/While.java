package pl.interpreter.parser.ast;

public record While(Parentheses parentheses, Instruction instruction) implements CompoundStatement {}
