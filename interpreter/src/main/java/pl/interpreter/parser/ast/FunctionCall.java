package pl.interpreter.parser.ast;

import java.util.List;

public record FunctionCall(List<Value> values) implements Node, IdentifierValueApplier {}
