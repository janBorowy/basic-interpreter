package pl.interpreter.parser.ast;

public record MatchBranch(String type, String identifier, Instruction instruction) implements Node {}
