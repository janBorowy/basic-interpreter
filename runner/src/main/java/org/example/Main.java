package org.example;

import java.io.InputStreamReader;
import pl.interpreter.Token;
import pl.interpreter.TokenType;
import pl.interpreter.lexical_analyzer.LexicalAnalyzer;
import pl.interpreter.lexical_analyzer.LexicalAnalyzerException;

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
            try {
                token = analyzer.getNextToken();
                System.out.println(token);
            } catch (LexicalAnalyzerException e) {
                System.out.println("Error: " + e.getMessage() + " at col " + e.getErrorCol() + ", row " + e.getErrorRow());
                return;
            }
        } while (token.type() != TokenType.EOF);
    }
}