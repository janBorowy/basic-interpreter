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

public class LexicalAnalyzer {

    private final Character STRING_BORDER_CHARACTER = '"';

    final Reader reader;
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

    public Token getNextToken() throws IOException {
        if(buffer.isEmpty()) {
            readNext();
        }
        clearWhitespaces();
        if(isOperatorToken()) {
            return getOperatorToken();
        }
        if(isNumberToken()) {
            return getNumberToken();
        }
        if(isStringLiteralToken()) {
            return getStringLiteralToken();
        }
        return new Token(TokenType.EOF, null, cursorRow, cursorCol);
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

    private void readNext() throws IOException {
        buffer.add((char) reader.read());
    }

    private void clearWhitespaces() throws IOException{
        char firstCharacter = buffer.getFirst();
        while(Character.isWhitespace(firstCharacter)) {
            if(buffer.getFirst() == '\n') {
                cursorCol = 1;
                ++cursorRow;
            } else {
                ++cursorCol;
            }
            buffer.pollFirst();
            readNext();
            firstCharacter = buffer.getFirst();
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

    private Token getOperatorToken() throws IOException {
        readNext();
        Token output;
        var doublyOperatorVal = getBufferContentString();
        var isDoublyOperator = doublyTokens.containsKey(doublyOperatorVal);
        if(isDoublyOperator) {
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

    private Token getNumberToken() throws IOException{
        var pattern = TokenPatternsProvider.getNumberPattern();
        // TODO: add number literal length limit
        while(pattern.matcher(getBufferContentString()).matches() || buffer.getLast() == '.') {
            readNext();
        }
        var bufferContent = getBufferContentString();
        var numberStr = bufferContent.substring(0, bufferContent.length() - 1);
        var token = new Token(TokenType.CONSTANT, getNumberTokenValue(numberStr), cursorRow, cursorCol);
        pollBufferAndAdvance(bufferContent.length() - 1);
        return token;
    }

    private Object getNumberTokenValue(String numberStr) {
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

    private Token getStringLiteralToken() throws IOException{
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
}
