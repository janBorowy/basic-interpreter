package pl.interpreter.parser;

import pl.interpreter.lexical_analyzer.Token;
import pl.interpreter.lexical_analyzer.TokenType;

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
            throw new ParserException("Invalid token", token().row(), token().col());
        }
        return token();
    }

    protected ParserException getParserException(String message) {
        return new ParserException(message, token().row(), token().col());
    }

    protected void throwParserException(String message) {
        throw new ParserException(message, token().row(), token().col());
    }

    protected String parseMustBeIdentifier() {
        var id = (String) mustBe(TokenType.IDENTIFIER).value();
        consumeToken();
        return id;
    }
}
