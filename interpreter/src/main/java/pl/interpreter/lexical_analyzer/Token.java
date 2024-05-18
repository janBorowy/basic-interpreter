package pl.interpreter.lexical_analyzer;

public record Token(TokenType type, Object value, int row, int col) {}
