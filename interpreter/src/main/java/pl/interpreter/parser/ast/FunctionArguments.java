package pl.interpreter.parser.ast;

import java.util.List;

public record FunctionArguments(List<Value> values) implements Node {}
