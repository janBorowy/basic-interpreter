package pl.interpreter.parser.ast;

import java.util.List;

public record Block(List<Node> statements) implements Node {}
