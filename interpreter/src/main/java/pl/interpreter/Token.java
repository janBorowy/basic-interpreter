package pl.interpreter;

public record Token(TokenType type, Object value, int row, int col) {}
