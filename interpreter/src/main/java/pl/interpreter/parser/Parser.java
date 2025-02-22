package pl.interpreter.parser;

import pl.interpreter.Token;
import pl.interpreter.TokenType;

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
            throw new ParserException("Invalid token at row: %d, col: %d".formatted(token().row(), token().col()), token().row(), token().col());
        }
        return token();
    }

    protected ParserException getParserException(String message) {
        return new ParserException(message.concat(": %d, col: %d".formatted(token().row(), token().col())), token().row(), token().col());
    }

    protected void throwParserException(String message) {
        throw new ParserException(message.concat(": %d, col: %d".formatted(token().row(), token().col())), token().row(), token().col());
    }

    protected String parseMustBeIdentifier() {
        var id = (String) mustBe(TokenType.IDENTIFIER).value();
        consumeToken();
        return id;
    }
}
