package org.example;

import java.io.InputStreamReader;
import pl.interpreter.Token;
import pl.interpreter.TokenType;
import pl.interpreter.lexical_analyzer.LexicalAnalyzer;

public class Main {
    /**
     * Tokenizes standard input
     * @param args
     */
    public static void main(String[] args) {
        var reader = new InputStreamReader(System.in);
        var analyzer = new LexicalAnalyzer(reader);
        Token token;
        do {
            token = analyzer.getNextToken();
            System.out.println(token);
        } while (token.type() != TokenType.EOF);
    }
}