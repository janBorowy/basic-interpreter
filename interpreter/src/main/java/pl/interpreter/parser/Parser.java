package pl.interpreter.parser;

import pl.interpreter.Token;
import pl.interpreter.TokenType;
import pl.interpreter.lexical_analyzer.LexicalAnalyzer;

public class Parser {

    private final TokenManager tokenManager;

    public Parser(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    protected Token token() {
        return tokenManager.getCurrentToken();
    }

    protected void consumeToken() {
        tokenManager.next();
    }

    protected Position getTokenPosition() {
        return new Position(token().row(), token().col());
    }

    protected boolean tokenIsOfType(TokenType type) {
        return token().type() == type;
    }

    protected Token mustBe(TokenType tokenType) {
        if (token().type() != tokenType) {
            throw new ParserException("Invalid token at row: %d, col: %d".formatted(token().row(), token().col()));
        }
        return token();
    }

    protected void throwParserError(String message) {
        throw new ParserException(message.concat(": %d, col: %d".formatted(token().row(), token().col())));
    }
}
