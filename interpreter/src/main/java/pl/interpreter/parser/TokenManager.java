package pl.interpreter.parser;

import pl.interpreter.Token;
import pl.interpreter.TokenType;
import pl.interpreter.lexical_analyzer.LexicalAnalyzer;

public class TokenManager {

    private final LexicalAnalyzer lexicalAnalyzer;
    private Token token;

    public TokenManager(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
        next();
    }

    public Token getCurrentToken() {
        return token;
    }

    public Token next() {
        do {
            token = lexicalAnalyzer.getNextToken();
        } while (token.type() == TokenType.COMMENT);
        return token;
    }
}
