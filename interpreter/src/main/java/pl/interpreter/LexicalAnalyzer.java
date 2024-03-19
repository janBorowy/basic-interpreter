package pl.interpreter;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;

public class LexicalAnalyzer {

    final Reader reader;
    private int cursorRow;
    private int cursorCol;

    private final Set<String> keywords = Set.of(
            "struct",
            "variant",
            "return",
            "while",
            "match",
            "if",
            "else",
            "as",
            "void",
            "int",
            "float",
            "string",
            "bool");

    private final Map<Character, TokenType> singlySymbols = Map.of(
            '+', TokenType.ADDITIVE_OPERATOR,
            '-', TokenType.ADDITIVE_OPERATOR,
            '*', TokenType.MULTIPLICATIVE_OPERATOR,
            '/', TokenType.MULTIPLICATIVE_OPERATOR,
            '%', TokenType.MULTIPLICATIVE_OPERATOR,
            '.', TokenType.COMMA,
            '<', TokenType.RELATIONAL_OPERATOR,
            '>', TokenType.RELATIONAL_OPERATOR,
            '!', TokenType.RELATIONAL_OPERATOR
    );

    private final Set<String> doublySymbols = Set.of(
            "<=",
            ">=",
            "!=",
            "=="
    );

    public LexicalAnalyzer(@NonNull Reader reader) {
        this.reader = reader;
        this.cursorCol = 1;
        this.cursorRow = 1;
    }

    public Token getNextToken() throws IOException {
        var character = (char) reader.read();

        while(Character.isWhitespace(character)) {
            if(character == '\n') {
                ++cursorRow;
            } else {
                ++cursorCol;
            }
            character = (char) reader.read();
        }

        if(isSinglyToken(character)) {
            return getSinglyTokenAndAdvance(character);
        }

        var tokenBuilder = new StringBuilder();
        tokenBuilder.append(character);
        tokenBuilder.append((char) reader.read());


        if(isDoublyToken(tokenBuilder.toString())) {
            cursorCol += 2;
            return getDoublyTokenAndAdvance(tokenBuilder.toString());
        }

        return new Token(TokenType.EOF, null, cursorRow, cursorCol);
    }

    private boolean isSinglyToken(char character) {
        return singlySymbols.containsKey(character);
    }

    private Token getSinglyTokenAndAdvance(char tokenValue) {
        var token =  new Token(singlySymbols.get(tokenValue), tokenValue, cursorRow, cursorCol);
        cursorCol += 1;
        return token;
    }

    private boolean isDoublyToken(String str) {
        return doublySymbols.contains(str);
    }

    private Token getDoublyTokenAndAdvance(String tokenValue) {
        var token = new Token(TokenType.RELATIONAL_OPERATOR, tokenValue, cursorRow, cursorCol);
        cursorCol += 2;
        return token;
    }
}
