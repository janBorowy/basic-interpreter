package pl.interpreter.parser;

import pl.interpreter.Token;
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
        token = lexicalAnalyzer.getNextToken();
        return token;
    }
}
