package pl.interpreter.parser.ast;

import java.util.Optional;

public record Return(Optional<Value> value) implements Node {}
