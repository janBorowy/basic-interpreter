package pl.interpreter.parser.ast;

import java.util.List;

public record Match(List<MatchBranch> branches) implements CompoundStatement {}
