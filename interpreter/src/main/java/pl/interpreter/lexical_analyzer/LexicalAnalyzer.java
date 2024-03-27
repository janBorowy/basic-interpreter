package pl.interpreter.lexical_analyzer;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.NonNull;
import pl.interpreter.Token;
import pl.interpreter.TokenType;

public class LexicalAnalyzer {

    private static final int MAX_WORD_LENGTH_LIMIT = 256;
    private static final Character STRING_BORDER_CHARACTER = '"';

    final Reader reader;
    private char lastCharacterRead;
    private int cursorRow;
    private int cursorCol;

    private final Map<String, TokenType> keywords;

    private final Map<Character, Supplier<Token>> operatorTokenSuppliers;

    public LexicalAnalyzer(@NonNull Reader reader) {
        this.reader = reader;
        readNext();
        cursorCol = 1;
        cursorRow = 1;
        keywords = new HashMap<>();
        prepareKeywords();
        operatorTokenSuppliers = new HashMap<>();
        prepareOperatorTokenSuppliers();
    }

    public Token getNextToken() {
        clearWhitespacesUntilFirstCharacterRead();
        if (lastCharacterRead == 0xFFFF) {
            return new Token(TokenType.EOF, null, cursorRow, cursorCol);
        }
        Optional<Token> optionalToken = tryToBuildOperatorToken();
        if (optionalToken.isPresent()) {
            return optionalToken.get();
        }
        optionalToken = tryToBuildNumberToken();
        if (optionalToken.isPresent()) {
            return optionalToken.get();
        }
        optionalToken = tryToBuildStringToken();
        if (optionalToken.isPresent()) {
            return optionalToken.get();
        }
        optionalToken = tryToBuildWordToken();
        if (optionalToken.isPresent()) {
            return optionalToken.get();
        }
        throw new LexicalAnalyzerException("Illegal character found.", cursorRow, cursorCol);
    }

    private void prepareKeywords() {
        keywords.put("struct", TokenType.KW_STRUCT);
        keywords.put("variant", TokenType.KW_VARIANT);
        keywords.put("return", TokenType.KW_RETURN);
        keywords.put("while", TokenType.KW_WHILE);
        keywords.put("match", TokenType.KW_MATCH);
        keywords.put("if", TokenType.KW_IF);
        keywords.put("else", TokenType.KW_ELSE);
        keywords.put("as", TokenType.KW_AS);
        keywords.put("void", TokenType.KW_VOID);
        keywords.put("int", TokenType.KW_INT);
        keywords.put("float", TokenType.KW_FLOAT);
        keywords.put("string", TokenType.KW_STRING);
        keywords.put("bool", TokenType.KW_BOOL);
        keywords.put("true", TokenType.KW_TRUE);
        keywords.put("false", TokenType.KW_FALSE);
    }
    private void prepareOperatorTokenSuppliers() {
        operatorTokenSuppliers.put('+', () -> new Token(TokenType.ADD_OPERATOR, '+', cursorRow, cursorCol));
        operatorTokenSuppliers.put('-', () -> chooseOperatorToken('>',
                new Token(TokenType.SUBTRACT_OPERATOR, '-', cursorRow, cursorCol),
                new Token(TokenType.ARROW, "->", cursorRow, cursorCol)));
        operatorTokenSuppliers.put('*', () -> new Token(TokenType.MULTIPLY_OPERATOR, '*', cursorRow, cursorCol));
        operatorTokenSuppliers.put('/', this::supplyForwardDash);
        operatorTokenSuppliers.put('%', () -> new Token(TokenType.MODULO_OPERATOR, '%', cursorRow, cursorCol));
        operatorTokenSuppliers.put(',', () -> new Token(TokenType.COMMA, ',', cursorRow, cursorCol));
        operatorTokenSuppliers.put('.', () -> new Token(TokenType.DOT, '.', cursorRow, cursorCol));
        operatorTokenSuppliers.put('<', () -> chooseOperatorToken('=',
                new Token(TokenType.LESS_THAN_OPERATOR, '<', cursorRow, cursorCol),
                new Token(TokenType.LESS_THAN_OR_EQUALS_OPERATOR, "<=", cursorRow, cursorCol)));
        operatorTokenSuppliers.put('>', () -> chooseOperatorToken('=',
                new Token(TokenType.GREATER_THAN_OPERATOR, '>', cursorRow, cursorCol),
                new Token(TokenType.GREATER_THAN_OR_EQUALS_OPERATOR, ">=", cursorRow, cursorCol)));
        operatorTokenSuppliers.put('!', () -> chooseOperatorToken('=',
                new Token(TokenType.NEGATION_OPERATOR, '!', cursorRow, cursorCol),
                new Token(TokenType.NOT_EQUALS_OPERATOR, "!=", cursorRow, cursorCol)));
        operatorTokenSuppliers.put('=', () -> chooseOperatorToken('=',
                new Token(TokenType.ASSIGNMENT, '=', cursorRow, cursorCol),
                new Token(TokenType.EQUALS_OPERATOR, "==", cursorRow, cursorCol)));
        operatorTokenSuppliers.put('(', () -> new Token(TokenType.LEFT_PARENTHESES, '(', cursorRow, cursorCol));
        operatorTokenSuppliers.put(')', () -> new Token(TokenType.RIGHT_PARENTHESES, ')', cursorRow, cursorCol));
        operatorTokenSuppliers.put('{', () -> new Token(TokenType.LEFT_CURLY_BRACKET, '{', cursorRow, cursorCol));
        operatorTokenSuppliers.put('}', () -> new Token(TokenType.RIGHT_CURLY_BRACKET, '}', cursorRow, cursorCol));
        operatorTokenSuppliers.put(';', () -> new Token(TokenType.SEMICOLON, ';', cursorRow, cursorCol));
    }

    private Token supplyForwardDash() {
        return switch (lastCharacterRead) {
            case '/' -> getLineCommentToken();
            case '*' -> getMultilineCommentToken();
            default -> new Token(TokenType.DIVIDE_OPERATOR, '/', cursorRow, cursorCol);
        };
    }

    private Token getLineCommentToken() {
        var cursorColIncrement = 0;
        while (lastCharacterRead != 0xFFFF && lastCharacterRead != '\n') {
            ++cursorColIncrement;
            readNext();
        }
        var token = new Token(TokenType.COMMENT, null, cursorRow, cursorCol);
        cursorCol += cursorColIncrement;
        if(lastCharacterRead == '\n') {
            cursorCol = 1;
            ++cursorRow;
        }
        readNext();
        return token;
    }

    private Token getMultilineCommentToken() {
        var lastCharBeforeRead = ' ';
        readNext();
        var newCursorCol = cursorCol;
        var cursorRowIncrement = 0;
        while (!concatChars(lastCharBeforeRead, lastCharacterRead).equals("*/")) {
            if (lastCharacterRead == '\n') {
                newCursorCol = 1;
                ++cursorRowIncrement;
            } else {
                ++newCursorCol;
            }
            lastCharBeforeRead = lastCharacterRead;
            readNext();
        }
        var token = new Token(TokenType.COMMENT, null, cursorCol, cursorRow);
        cursorCol = newCursorCol + 3;
        cursorRow += cursorRowIncrement;
        readNext();
        return token;
    }

    private String concatChars(char a, char b) {
        return String.valueOf(a) + b;
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

    private Optional<Token> tryToBuildOperatorToken() {
        if(!operatorTokenSuppliers.containsKey(lastCharacterRead)) {
            return Optional.empty();
        }

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
        return Optional.of(token);
    }

    private Optional<Token> tryToBuildNumberToken() {
        if(!Character.isDigit(lastCharacterRead)) {
            return Optional.empty();
        }
        var builder = new StringBuilder();
        while (Character.isDigit(lastCharacterRead)) {
            builder.append(lastCharacterRead);
            readNext();
        }
        var integralValue = Integer.parseInt(builder.toString());
        if (lastCharacterRead == '.') {
            var decimalBuilder = new StringBuilder();
            readNext();
            while (Character.isDigit(lastCharacterRead)) {
                decimalBuilder.append(lastCharacterRead);
                readNext();
            }
            var decimalValue = tryToParseInt(decimalBuilder.toString());
            var token = new Token(TokenType.FLOAT_CONST, computeFloat(integralValue, decimalValue, decimalBuilder.length()), cursorRow, cursorCol);
            cursorCol += builder.length() + decimalBuilder.length() + 1;
            return Optional.of(token);
        }
        var token = new Token(TokenType.INT_CONST, integralValue, cursorRow, cursorCol);
        cursorCol += builder.length();
        return Optional.of(token);
    }

    private int tryToParseInt(String strValue) {
        try {
            return Integer.parseInt(strValue);
        } catch (NumberFormatException e) {
            throw new LexicalAnalyzerException("Failed to parse a number", cursorCol, cursorRow);
        }
    }

    private float computeFloat(int integral, int decimal, int decimalLength) {
        return (float) (integral + (decimal / Math.pow(10, decimalLength)));
    }

    private Optional<Token> tryToBuildStringToken() {
        if(!STRING_BORDER_CHARACTER.equals(lastCharacterRead)) {
            return Optional.empty();
        }
        var builder = new StringBuilder();
        var escapeNext = false;
        var backslashCounter = 0;
        readNext();
        while (!STRING_BORDER_CHARACTER.equals(lastCharacterRead) || escapeNext) {
            if(lastCharacterRead == '\\' && !escapeNext) {
                escapeNext = true;
                ++backslashCounter;
                readNext();
                continue;
            }
            escapeNext = false;
            builder.append(lastCharacterRead);
            readNext();
        }
        readNext();
        var token = new Token(TokenType.STRING_CONST, builder.toString(), cursorRow, cursorCol);
        cursorCol += builder.length() + backslashCounter + 2;
        return Optional.of(token);
    }

    private boolean isLegalWordCharacter(Character c) {
        return c == '_' || Character.isLetter(c);
    }

    private Optional<Token> tryToBuildWordToken() {
        if(!isLegalWordCharacter(lastCharacterRead)) {
            return Optional.empty();
        }
        var builder = new StringBuilder();
        var counter = 0;
        builder.append(lastCharacterRead);
        readNext();
        while (isLegalWordCharacter(lastCharacterRead) && counter < MAX_WORD_LENGTH_LIMIT) {
            builder.append(lastCharacterRead);
            ++counter;
            readNext();
        }
        if(counter >= MAX_WORD_LENGTH_LIMIT) {
            throw new LexicalAnalyzerException("Identifier length limit exceeded", cursorCol, cursorRow);
        }
        var tokenValue = builder.toString();
        var token = new Token(getWordTokenType(tokenValue), tokenValue, cursorRow, cursorCol);
        cursorCol += builder.length();
        return Optional.of(token);
    }

    private TokenType getWordTokenType(String tokenValue) {
        return keywords.getOrDefault(tokenValue, TokenType.IDENTIFIER);
    }
}
