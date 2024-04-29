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

    private static final int MAX_INTEGER_VALUE = Integer.MAX_VALUE;
    private static final int MAX_WORD_LENGTH_LIMIT = 256;
    private static final int MAX_FLOAT_PRECISION = 7;
    private static final Character STRING_BORDER_CHARACTER = '"';
    private static final Character RADIX_POINT = '.';

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
        keywords = LexicalAnalysisStaticProvider.getKeywords();
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
        } else {
            throw new LexicalAnalyzerException("Could not build operator token", cursorRow, cursorCol);
        }
        return Optional.of(token);
    }

    private Optional<Token> tryToBuildNumberToken() {
        if(!Character.isDigit(lastCharacterRead)) {
            return Optional.empty();
        }

        int number = Character.getNumericValue(lastCharacterRead);
        int numberDigits = 1;
        readNext();
        if(number != 0) {
            while (Character.isDigit(lastCharacterRead)) {
                int lastDigitRead = Character.getNumericValue(lastCharacterRead);
                checkDoesNotExceedLimit(number, lastDigitRead);
                number = number * 10 + lastDigitRead;
                ++numberDigits;
                readNext();
            }
        }

        if (lastCharacterRead != RADIX_POINT) {
            var token = Optional.of(new Token(TokenType.INT_CONST, number, cursorRow, cursorCol));
            cursorCol += numberDigits;
            return token;
        }
        readNext();

        if (!Character.isDigit(lastCharacterRead)) {
            throw new LexicalAnalyzerException("Invalid float literal" , cursorRow, cursorCol);
        }

        int decimalNumber = 0;
        int decimalPrecision = 0;
        while (Character.isDigit(lastCharacterRead)) {
            int lastDigitRead = Character.getNumericValue(lastCharacterRead);
            checkDoesNotExceedLimit(decimalNumber, lastDigitRead);
            checkDoesNotExceedMaxPrecision(decimalPrecision);
            decimalNumber = decimalNumber * 10 + lastDigitRead;
            ++decimalPrecision;
            readNext();
        }
        float floatNumber = (float) (number + decimalNumber / Math.pow(10, decimalPrecision));
        var token = Optional.of(new Token(TokenType.FLOAT_CONST, floatNumber, cursorRow, cursorCol));
        cursorCol += numberDigits + 1 + decimalPrecision;
        return token;
    }

    private void checkDoesNotExceedLimit(int number, int newDigit) {
        if ((MAX_INTEGER_VALUE - newDigit) / 10 < number) {
            throw new LexicalAnalyzerException("Integer is too large", cursorRow, cursorCol);
        }
    }

    private void checkDoesNotExceedMaxPrecision(int decimalPrecision) {
        if (MAX_FLOAT_PRECISION < decimalPrecision) {
            throw new LexicalAnalyzerException("Float precision is too big", cursorRow, cursorCol);
        }
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
            if(lastCharacterRead == 0xFFFF || lastCharacterRead == '\n') {
                throw new LexicalAnalyzerException("Expected '\"'" , cursorCol, cursorRow);
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
            throw new LexicalAnalyzerException("Identifier length limit exceeded", cursorRow, cursorCol);
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
