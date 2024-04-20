package pl.interpreter.parser.ast;

import java.util.List;

public record Subcondition(List<BooleanExpression> expressions) implements Node {}
