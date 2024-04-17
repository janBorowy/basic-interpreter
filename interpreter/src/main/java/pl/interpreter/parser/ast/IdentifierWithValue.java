package pl.interpreter.parser.ast;

import java.util.Optional;

public record IdentifierWithValue(String value, Optional<IdentifierValueApplier> applier) implements Factor {}
