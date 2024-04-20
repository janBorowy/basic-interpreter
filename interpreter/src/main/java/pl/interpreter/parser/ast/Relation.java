package pl.interpreter.parser.ast;

import java.util.Optional;

public record Relation(Expression first, Optional<ArithmeticCondition> arithmeticCondition, Optional<Expression> second) implements LogicTerm {}
