package pl.interpreter.parser.ast;

import java.util.List;

public record Parentheses(List<Subcondition> subconditions) implements ParenthesesValue {}
