package pl.interpreter.parser.ast;

import java.util.List;

public record VariantDefinition(String identifier, List<String> variantIdentifiers) implements Definition {}
