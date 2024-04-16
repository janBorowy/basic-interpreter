package pl.interpreter.parser.ast;

import java.util.List;

public record Program(List<Definition> definitions) implements Node {}
