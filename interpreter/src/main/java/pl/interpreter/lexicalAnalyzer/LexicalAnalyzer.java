package pl.interpreter.lexicalAnalyzer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.NonNull;
import pl.interpreter.Token;
import pl.interpreter.TokenType;

// TODO: sprawdzić funkcjie javki
// TODO: escape character
// TODO: comments

public class LexicalAnalyzer {

    private static final int MAX_WORD_LENGTH_LIMIT = 256;
    private static final Character STRING_BORDER_CHARACTER = '"';

    final Reader reader;
    // TODO: maybe change to stack?
    // Albo wgl to wywalić, patrzeć tylko na 1 znak i do budowania numerów i stringów używać StringBuildera
    private final Deque<Character> buffer;
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

    private final Map<Character, TokenType> singlyTokens;
    private final Map<String, TokenType> doublyTokens;

    public LexicalAnalyzer(@NonNull Reader reader) {
        this.reader = reader;
        buffer = new ArrayDeque<>();
        cursorCol = 1;
        cursorRow = 1;
        singlyTokens = new HashMap<>();
        doublyTokens = new HashMap<>();
        prepareSinglyTokens();
        prepareDoublyTokens();
    }

    public Token getNextToken() {
        if(buffer.isEmpty()) {
            readNext();
        }
        clearWhitespacesUntilFirstCharacterRead();
        if(buffer.getFirst() == 0xFFFF) {
            return new Token(TokenType.EOF, null, cursorRow, cursorCol);
        }
        // TODO: tryBuild lepsze
        // TODO: Zamienic drabinke
        if(isOperatorToken()) {
            return getOperatorToken();
        }
        if(isNumberToken()) {
            return getNumberToken();
        }
        if(isStringLiteralToken()) {
            return getStringLiteralToken();
        }
        if(isWordToken()) {
            return getWordToken();
        }
        throw new IllegalCharacterException();
    }

    private void prepareSinglyTokens() {
        singlyTokens.put('+', TokenType.ADDITIVE_OPERATOR);
        singlyTokens.put('-', TokenType.ADDITIVE_OPERATOR);
        singlyTokens.put('*', TokenType.MULTIPLICATIVE_OPERATOR);
        singlyTokens.put('/', TokenType.MULTIPLICATIVE_OPERATOR);
        singlyTokens.put('%', TokenType.MULTIPLICATIVE_OPERATOR);
        singlyTokens.put('.', TokenType.COMMA);
        singlyTokens.put('<', TokenType.RELATIONAL_OPERATOR);
        singlyTokens.put('>', TokenType.RELATIONAL_OPERATOR);
        singlyTokens.put('!', TokenType.RELATIONAL_OPERATOR);
        singlyTokens.put('=', TokenType.ASSIGNMENT);
        singlyTokens.put('(', TokenType.LEFT_PARENTHESES);
        singlyTokens.put(')', TokenType.RIGHT_PARENTHESES);
        singlyTokens.put('{', TokenType.LEFT_CURLY_BRACKET);
        singlyTokens.put('}', TokenType.RIGHT_CURLY_BRACKET);
        singlyTokens.put(';', TokenType.SEMICOLON);
    }

    private void prepareDoublyTokens() {
        doublyTokens.put("<=", TokenType.RELATIONAL_OPERATOR);
        doublyTokens.put(">=", TokenType.RELATIONAL_OPERATOR);
        doublyTokens.put("!=", TokenType.RELATIONAL_OPERATOR);
        doublyTokens.put("==", TokenType.RELATIONAL_OPERATOR);
        doublyTokens.put("->", TokenType.ARROW);
    }

    private void readNext() {
        try {
            buffer.add((char) reader.read());
        } catch(IOException e) {
            throw new InterpreterIOException(e.getMessage());
        }
    }

    private void clearWhitespacesUntilFirstCharacterRead() {
        while(Character.isWhitespace(buffer.getFirst())) {
            readNext();
            // TODO: support other newline types
            if (buffer.getFirst() == '\n') {
                cursorCol = 1;
                ++cursorRow;
            } else {
                ++cursorCol;
            }
            buffer.pollFirst();
        }
    }

    private void pollBufferAndAdvance() {
        pollBufferAndAdvance(1);
    }

    private void pollBufferAndAdvance(int pollCount) {
        cursorCol += pollCount;
        IntStream.range(0, pollCount).forEach(i -> buffer.pollFirst());
    }

    private void clearBufferAndAdvance() {
        cursorCol += buffer.size();
        buffer.clear();
    }

    private boolean isOperatorToken() {
        return singlyTokens.containsKey(buffer.getFirst());
    }

    private Token getOperatorToken() {
        readNext();
        Token output;
        var doublyOperatorVal = getBufferContentString();
        if(doublyTokens.containsKey(doublyOperatorVal)) {
            output = new Token(doublyTokens.get(doublyOperatorVal), doublyOperatorVal, cursorRow, cursorCol);
            clearBufferAndAdvance();
        } else {
            output = new Token(singlyTokens.get(buffer.getFirst()), buffer.getFirst(), cursorRow, cursorCol);
            pollBufferAndAdvance();
        }
        return output;
    }

    private String getBufferContentString() {
        return buffer.stream().map(String::valueOf).collect(Collectors.joining());
    }

    private boolean isNumberToken() {
        return Character.isDigit(buffer.getFirst());
    }

    private Token getNumberToken() {
        // TODO: update so that 21.37. teokenizes to FLOAT - DOT
        var pattern = TokenPatternsProvider.getNumberPattern();
        while(pattern.matcher(getBufferContentString()).matches() || buffer.getLast() == '.') {
            readNext();
        }
        var numberStr = getBufferContentExceptLastCharacter();
        var token = new Token(TokenType.CONSTANT, getNumberTokenValue(numberStr), cursorRow, cursorCol);
        pollBufferAndAdvance(numberStr.length());
        return token;
    }

    private Object getNumberTokenValue(String numberStr) {
        // TODO: nie przepuszczać 0 na początku
        if(numberStr.endsWith(".")) {
            throw new NumberTokenizationException();
        }
        if (numberStr.contains(".")) {
            return Float.valueOf(numberStr);
        } else {
            return Integer.valueOf(numberStr);
        }
    }

    private boolean isStringLiteralToken() {
        return STRING_BORDER_CHARACTER.equals(buffer.getFirst());
    }

    private Token getStringLiteralToken() {
        // TODO: add backslash escape mechanism
        readNext();
        while(!STRING_BORDER_CHARACTER.equals(buffer.getLast())) {
            readNext();
        }
        var tokenValueWithQuotes = getBufferContentString();
        var tokenValue = tokenValueWithQuotes.substring(1, tokenValueWithQuotes.length() - 1);
        var token = new Token(TokenType.CONSTANT, tokenValue, cursorRow, cursorCol);
        clearBufferAndAdvance();
        return token;
    }

    private boolean isWordToken() {
        return isLegalWordCharacter(buffer.getFirst());
    }

    private boolean isLegalWordCharacter(Character c) {
        return c == '_' | Character.isLetter(c);
    }

    private Token getWordToken() {
        readNext();
        int readCounter = 1;
        while(isLegalWordCharacter(buffer.getLast()) && readCounter <= MAX_WORD_LENGTH_LIMIT) {
            readNext();
            ++readCounter;
        }
        var tokenValue = getBufferContentExceptLastCharacter();
        var token = new Token(getWordTokenType(tokenValue), tokenValue, cursorRow, cursorCol);
        pollBufferAndAdvance(tokenValue.length());
        return token;
    }

    private String getBufferContentExceptLastCharacter() {
        var bufferContentString = getBufferContentString();
        return bufferContentString.substring(0, bufferContentString.length() - 1);
    }

    private TokenType getWordTokenType(String tokenValue) {
        return keywords.contains(tokenValue) ? TokenType.KEYWORD : TokenType.IDENTIFIER;
    }
}
