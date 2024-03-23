package pl.interpreter.lexicalAnalyzer;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import lombok.NonNull;
import pl.interpreter.Token;
import pl.interpreter.TokenType;

// TODO: sprawdzić funkcje javki
// TODO: escape character
// TODO: comments

public class LexicalAnalyzer {

    private static final int MAX_WORD_LENGTH_LIMIT = 256;
    private static final Character STRING_BORDER_CHARACTER = '"';

    final Reader reader;
    private char lastCharacterRead;
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

    private final Map<Character, Supplier<Token>> operatorTokenSuppliers;

    public LexicalAnalyzer(@NonNull Reader reader) {
        this.reader = reader;
        readNext();
        cursorCol = 1;
        cursorRow = 1;
        operatorTokenSuppliers = new HashMap<>();
        prepareOperatorTokenSuppliers();
    }

    public Token getNextToken() {
        clearWhitespacesUntilFirstCharacterRead();
        if (lastCharacterRead == 0xFFFF) {
            return new Token(TokenType.EOF, null, cursorRow, cursorCol);
        }
        if (isOperatorToken()) {
            return getOperatorToken();
        }
        if (isNumberToken()) {
            return getNumberToken();
        }
        if (isStringLiteralToken()) {
            return getStringLiteralToken();
        }
        if (isWordToken()) {
            return getWordToken();
        }
        throw new IllegalCharacterException();
    }

    private void prepareOperatorTokenSuppliers() {
        operatorTokenSuppliers.put('+', () -> new Token(TokenType.ADDITIVE_OPERATOR, '+', cursorRow, cursorCol));
        operatorTokenSuppliers.put('-', () -> chooseOperatorToken('>',
                new Token(TokenType.ADDITIVE_OPERATOR, '-', cursorRow, cursorCol),
                new Token(TokenType.ARROW, "->", cursorRow, cursorCol)));
        operatorTokenSuppliers.put('*', () -> new Token(TokenType.MULTIPLICATIVE_OPERATOR, '*', cursorRow, cursorCol));
        operatorTokenSuppliers.put('/', () -> new Token(TokenType.MULTIPLICATIVE_OPERATOR, '/', cursorRow, cursorCol));
        operatorTokenSuppliers.put('%', () -> new Token(TokenType.MULTIPLICATIVE_OPERATOR, '%', cursorRow, cursorCol));
        operatorTokenSuppliers.put(',', () -> new Token(TokenType.COMMA, ',', cursorRow, cursorCol));
        operatorTokenSuppliers.put('.', () -> new Token(TokenType.DOT, '.', cursorRow, cursorCol));
        operatorTokenSuppliers.put('<', () -> chooseOperatorToken('=',
                new Token(TokenType.RELATIONAL_OPERATOR, '<', cursorRow, cursorCol),
                new Token(TokenType.RELATIONAL_OPERATOR, "<=", cursorRow, cursorCol)));
        operatorTokenSuppliers.put('>', () -> chooseOperatorToken('=',
                new Token(TokenType.RELATIONAL_OPERATOR, '>', cursorRow, cursorCol),
                new Token(TokenType.RELATIONAL_OPERATOR, ">=", cursorRow, cursorCol)));
        operatorTokenSuppliers.put('!', () -> chooseOperatorToken('=',
                new Token(TokenType.RELATIONAL_OPERATOR, '!', cursorRow, cursorCol),
                new Token(TokenType.RELATIONAL_OPERATOR, "!=", cursorRow, cursorCol)));
        operatorTokenSuppliers.put('=', () -> chooseOperatorToken('=',
                new Token(TokenType.ASSIGNMENT, '=', cursorRow, cursorCol),
                new Token(TokenType.RELATIONAL_OPERATOR, "==", cursorRow, cursorCol)));
        operatorTokenSuppliers.put('(', () -> new Token(TokenType.LEFT_PARENTHESES, '(', cursorRow, cursorCol));
        operatorTokenSuppliers.put(')', () -> new Token(TokenType.RIGHT_PARENTHESES, ')', cursorRow, cursorCol));
        operatorTokenSuppliers.put('{', () -> new Token(TokenType.LEFT_CURLY_BRACKET, '{', cursorRow, cursorCol));
        operatorTokenSuppliers.put('}', () -> new Token(TokenType.RIGHT_CURLY_BRACKET, '}', cursorRow, cursorCol));
        operatorTokenSuppliers.put(';', () -> new Token(TokenType.SEMICOLON, ';', cursorRow, cursorCol));
    }

    private Token chooseOperatorToken(char secondOperatorChar, Token ifIsSingly, Token ifIsDoubly) {
        return lastCharacterRead == secondOperatorChar ? ifIsDoubly : ifIsSingly;
    }

    private void readNext() {
        try {
            lastCharacterRead = (char) reader.read();
        } catch (IOException e) {
            throw new InterpreterIOException(e.getMessage());
        }
    }

    private void clearWhitespacesUntilFirstCharacterRead() {
        while (Character.isWhitespace(lastCharacterRead)) {
            if (lastCharacterRead == '\n') {
                cursorCol = 1;
                ++cursorRow;
            } else {
                ++cursorCol;
            }
            readNext();
        }
    }

    private boolean isOperatorToken() {
        return operatorTokenSuppliers.containsKey(lastCharacterRead);
    }

    private Token getOperatorToken() {
        var firstCharacter = lastCharacterRead;
        readNext();
        var token = operatorTokenSuppliers.get(firstCharacter).get();
        var tokenValue = token.value();
        if (tokenValue instanceof String s) {
            cursorCol += s.length();
            readNext();
        } else if (tokenValue instanceof Character) {
            cursorCol += 1;
        }
        return token;
    }

    private boolean isNumberToken() {
        return Character.isDigit(lastCharacterRead);
    }

    private Token getNumberToken() {
        // TODO: add max literal length limit
        var builder = new StringBuilder();
        while (Character.isDigit(lastCharacterRead) /*and doesnt break the limit */) {
            builder.append(lastCharacterRead);
            readNext();
        }
        var integralValue = Integer.parseInt(builder.toString());
        if (lastCharacterRead == '.') {
            var decimalBuilder = new StringBuilder();
            readNext();
            while (Character.isDigit(lastCharacterRead) /*and doesnt break the limit */) {
                decimalBuilder.append(lastCharacterRead);
                readNext();
            }
            var decimalValue = tryToParseInt(decimalBuilder.toString());
            var token = new Token(TokenType.CONSTANT, computeFloat(integralValue, decimalValue, decimalBuilder.length()), cursorRow, cursorCol);
            cursorCol += builder.length() + decimalBuilder.length() + 1;
            return token;
        }
        var token = new Token(TokenType.CONSTANT, integralValue, cursorRow, cursorCol);
        cursorCol += builder.length();
        return token;
    }

    private int tryToParseInt(String strValue) {
        try {
            return Integer.parseInt(strValue);
        } catch (NumberFormatException e) {
            throw new NumberTokenizationException();
        }
    }

    private float computeFloat(int integral, int decimal, int decimalLength) {
        return (float) (integral + (decimal / Math.pow(10, decimalLength)));
    }

    private boolean isStringLiteralToken() {
        return STRING_BORDER_CHARACTER.equals(lastCharacterRead);
    }

    private Token getStringLiteralToken() {
        // TODO: add backslash escape mechanism
        var builder = new StringBuilder();
        readNext();
        while (!STRING_BORDER_CHARACTER.equals(lastCharacterRead)) {
            builder.append(lastCharacterRead);
            readNext();
        }
        readNext();
        var token = new Token(TokenType.CONSTANT, builder.toString(), cursorRow, cursorCol);
        cursorCol += builder.length() + 2;
        return token;
    }

    private boolean isWordToken() {
        return isLegalWordCharacter(lastCharacterRead);
    }

    private boolean isLegalWordCharacter(Character c) {
        return c == '_' || Character.isLetter(c);
    }

    private Token getWordToken() {
        // TODO: add limit
        var builder = new StringBuilder();
        builder.append(lastCharacterRead);
        readNext();
        while (isLegalWordCharacter(lastCharacterRead)) {
            builder.append(lastCharacterRead);
            readNext();
        }
        var tokenValue = builder.toString();
        var token = new Token(getWordTokenType(tokenValue), tokenValue, cursorRow, cursorCol);
        cursorCol += builder.length();
        return token;
    }

    private TokenType getWordTokenType(String tokenValue) {
        return keywords.contains(tokenValue) ? TokenType.KEYWORD : TokenType.IDENTIFIER;
    }
}
