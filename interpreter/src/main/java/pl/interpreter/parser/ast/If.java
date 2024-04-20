package pl.interpreter.parser.ast;

import java.util.Optional;

public record If(Parentheses parentheses, Instruction instruction, Optional<Instruction> elseInstruction) implements CompoundStatement {}
