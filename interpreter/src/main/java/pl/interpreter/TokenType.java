package pl.interpreter;

public enum TokenType {
    // TODO: change token type
    KW_STRUCT,
    KW_VARIANT,
    KW_RETURN,
    KW_WHILE,
    KW_MATCH,
    KW_IF,
    KW_ELSE,
    KW_AS,
    KW_VOID,
    KW_INT,
    KW_FLOAT,
    KW_STRING,
    KW_BOOL,
    KW_TRUE,
    KW_FALSE,
    KW_AND,
    KW_OR,
    KW_VAR,
    IDENTIFIER,
    STRING_CONST,
    INT_CONST,
    FLOAT_CONST,
    LEFT_CURLY_BRACKET,
    RIGHT_CURLY_BRACKET,
    LEFT_PARENTHESES,
    RIGHT_PARENTHESES,
    SEMICOLON,
    COMMA,
    ARROW,
    ADD_OPERATOR,
    SUBTRACT_OPERATOR,
    MULTIPLY_OPERATOR,
    DIVIDE_OPERATOR,
    MODULO_OPERATOR,
    NEGATION_OPERATOR,
    EQUALS_OPERATOR,
    NOT_EQUALS_OPERATOR,
    LESS_THAN_OPERATOR,
    LESS_THAN_OR_EQUALS_OPERATOR,
    GREATER_THAN_OPERATOR,
    GREATER_THAN_OR_EQUALS_OPERATOR,
    ASSIGNMENT,
    DOT,
    EOF,
    COMMENT
}
