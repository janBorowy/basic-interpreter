package pl.interpreter

import pl.interpreter.lexical_analyzer.LexicalAnalyzer
import pl.interpreter.lexical_analyzer.LexicalAnalyzerException
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.IntStream

class LexicalAnalyzerSpec extends Specification {
    def static sourceCode1 = Path.of(
            "src",
            "test",
            "groovy",
            "pl",
            "interpreter",
            "testSourceCode",
            "source_code_1.lang")

    private static def getAllTokens(LexicalAnalyzer analyzer) {
        def tokens = new ArrayList<Token>()
        def token = null
        do {
            token = analyzer.getNextToken()
            tokens.add(token)
        } while (token.type() != TokenType.EOF)
        return tokens;
    }

    private static def tokenize(String code) {
        def reader = new StringReader(code)
        def lexicalAnalyzer = new LexicalAnalyzer(reader)
        return getAllTokens(lexicalAnalyzer)
    }

    def 'Should tokenize EOF'() {
        given:
        def code = ""
        expect:
        tokenize(code) == [new Token(TokenType.EOF, null, 1, 1)]
    }

    def 'Should tokenize singly tokens correctly'() {
        given:
        def code = "+ - * / % < > ! . = ( ) { } ;"

        expect:
        tokenize(code) == [
                new Token(TokenType.ADD_OPERATOR, '+' as char, 1, 1),
                new Token(TokenType.SUBTRACT_OPERATOR, '-' as char, 1, 3),
                new Token(TokenType.MULTIPLY_OPERATOR, '*' as char, 1, 5),
                new Token(TokenType.DIVIDE_OPERATOR, '/' as char, 1, 7),
                new Token(TokenType.MODULO_OPERATOR, '%' as char, 1, 9),
                new Token(TokenType.LESS_THAN_OPERATOR, '<' as char, 1, 11),
                new Token(TokenType.GREATER_THAN_OPERATOR, '>' as char, 1, 13),
                new Token(TokenType.NEGATION_OPERATOR, '!' as char, 1, 15),
                new Token(TokenType.DOT, '.' as char, 1, 17),
                new Token(TokenType.ASSIGNMENT, '=' as char, 1, 19),
                new Token(TokenType.LEFT_PARENTHESES, '(' as char, 1, 21),
                new Token(TokenType.RIGHT_PARENTHESES, ')' as char, 1, 23),
                new Token(TokenType.LEFT_CURLY_BRACKET, '{' as char, 1, 25),
                new Token(TokenType.RIGHT_CURLY_BRACKET, '}' as char, 1, 27),
                new Token(TokenType.SEMICOLON, ';' as char, 1, 29),
                new Token(TokenType.EOF, null, 1, 30)
        ]
    }

    def 'Should tokenize doubly tokens correctly'() {
        given:
        def code = "== != <= >= ->"
        expect:
        tokenize(code) == [
                new Token(TokenType.EQUALS_OPERATOR, "==", 1, 1),
                new Token(TokenType.NOT_EQUALS_OPERATOR, "!=", 1, 4),
                new Token(TokenType.LESS_THAN_OR_EQUALS_OPERATOR, "<=", 1, 7),
                new Token(TokenType.GREATER_THAN_OR_EQUALS_OPERATOR, ">=", 1, 10),
                new Token(TokenType.ARROW, "->", 1, 13),
                new Token(TokenType.EOF, null, 1, 15)
        ]
    }

    def 'Should show correct row, col values'() {
        given:
        def code = "== !=\n >= + \n   !\n--  \n"
        expect:
        tokenize(code) == [
                new Token(TokenType.EQUALS_OPERATOR, "==", 1, 1),
                new Token(TokenType.NOT_EQUALS_OPERATOR, "!=", 1, 4),
                new Token(TokenType.GREATER_THAN_OR_EQUALS_OPERATOR, ">=", 2, 2),
                new Token(TokenType.ADD_OPERATOR, '+' as char, 2, 5),
                new Token(TokenType.NEGATION_OPERATOR, '!' as char, 3, 4),
                new Token(TokenType.SUBTRACT_OPERATOR, '-' as char, 4, 1),
                new Token(TokenType.SUBTRACT_OPERATOR, '-' as char, 4, 2),
                new Token(TokenType.EOF, null, 5, 1)
        ]
    }

    def 'Should read number tokens correctly'() {
        given:
        def code = "1 1.5 21.37 0 0.75."
        expect:
        tokenize(code) == [
                new Token(TokenType.INT_CONST, Integer.valueOf(1), 1, 1),
                new Token(TokenType.FLOAT_CONST, Float.valueOf(1.5), 1, 3),
                new Token(TokenType.FLOAT_CONST, Float.valueOf(21.37), 1, 7),
                new Token(TokenType.INT_CONST, Integer.valueOf(0), 1, 13),
                new Token(TokenType.FLOAT_CONST, Float.valueOf(0.75), 1, 15),
                new Token(TokenType.DOT, '.' as char, 1, 19),
                new Token(TokenType.EOF, null, 1, 20)
        ]
    }

    def 'Should throw number tokenization exception when number was not ended correctly'() {
        given:
        def code = "1. 1"
        when:
        tokenize(code)
        then:
        LexicalAnalyzerException e = thrown()
    }

    def 'Should tokenize string literal correctly'() {
        given:
        def code = "\"hello world\""

        expect:
        tokenize(code) == [
                new Token(TokenType.STRING_CONST, "hello world", 1, 1),
                new Token(TokenType.EOF, null, 1, 14)
        ]
    }

    def 'Should tokenize multiple string literals correctly'() {
        given:
        def code = "\"   hello   \"\n\"world hello\""
        expect:
        tokenize(code) == [
                new Token(TokenType.STRING_CONST, "   hello   ", 1, 1),
                new Token(TokenType.STRING_CONST, "world hello", 2, 1),
                new Token(TokenType.EOF, null, 2, 14)
        ]
    }

    def 'Should tokenize identifiers and keywords correctly'() {
        given:
        def code = "hello world int"

        expect:
        tokenize(code) == [
                new Token(TokenType.IDENTIFIER, "hello", 1, 1),
                new Token(TokenType.IDENTIFIER, "world", 1, 7),
                new Token(TokenType.KW_INT, "int", 1, 13),
                new Token(TokenType.EOF, null, 1, 16)
        ]
    }

    def 'Should tokenize comments correctly'() {
        given:
        def code = "/*\ndemo software\n*/\nint a = b; // this is an assignment"

        expect:
        tokenize(code) == [
                new Token(TokenType.COMMENT, null, 1, 1),
                new Token(TokenType.KW_INT, "int", 4, 1),
                new Token(TokenType.IDENTIFIER, "a", 4, 5),
                new Token(TokenType.ASSIGNMENT, '=' as char, 4, 7),
                new Token(TokenType.IDENTIFIER, "b", 4, 9),
                new Token(TokenType.SEMICOLON, ';' as char, 4, 10),
                new Token(TokenType.COMMENT, null, 4, 12),
                new Token(TokenType.EOF, null, 4, 35)
        ]
    }

    def 'Should tokenize source_code_1 correctly'() {
        given:
        def reader = Files.newBufferedReader(sourceCode1)
        def lexicalAnalyzer = new LexicalAnalyzer(reader)
        def tokens = getAllTokens(lexicalAnalyzer)
        expect:
        tokens == [
                new Token(TokenType.COMMENT, null, 1, 1),

                new Token(TokenType.KW_INT, "int", 6, 1),
                new Token(TokenType.IDENTIFIER, "main", 6, 5),
                new Token(TokenType.LEFT_PARENTHESES, '(' as char, 6, 9),
                new Token(TokenType.RIGHT_PARENTHESES, ')' as char, 6, 10),
                new Token(TokenType.LEFT_CURLY_BRACKET, '{' as char, 6, 12),
                new Token(TokenType.COMMENT, null, 6, 14),

                new Token(TokenType.KW_INT, "int", 7, 5),
                new Token(TokenType.IDENTIFIER, "a", 7, 9),
                new Token(TokenType.ASSIGNMENT, '=' as char, 7, 11),
                new Token(TokenType.INT_CONST, 2, 7, 13),
                new Token(TokenType.SEMICOLON, ';' as char, 7, 14),
                new Token(TokenType.COMMENT, null, 7, 16),

                new Token(TokenType.KW_INT, "int", 8, 5),
                new Token(TokenType.IDENTIFIER, "b", 8, 9),
                new Token(TokenType.ASSIGNMENT, '=' as char, 8, 11),
                new Token(TokenType.INT_CONST, 2, 8, 13),
                new Token(TokenType.SEMICOLON, ';' as char, 8, 14),

                new Token(TokenType.KW_INT, "int", 9, 5),
                new Token(TokenType.IDENTIFIER, "sum", 9, 9),
                new Token(TokenType.ASSIGNMENT, '=' as char, 9, 13),
                new Token(TokenType.IDENTIFIER, "a", 9, 15),
                new Token(TokenType.ADD_OPERATOR, '+' as char, 9, 17),
                new Token(TokenType.IDENTIFIER, "b", 9, 19),
                new Token(TokenType.SEMICOLON, ';' as char, 9, 20),
                new Token(TokenType.COMMENT, null, 9, 22),

                new Token(TokenType.KW_STRING, "string", 10, 5),
                new Token(TokenType.IDENTIFIER, "str", 10, 12),
                new Token(TokenType.ASSIGNMENT, '=' as char, 10, 16),
                new Token(TokenType.LEFT_PARENTHESES, '(' as char, 10, 18),
                new Token(TokenType.INT_CONST, 2, 10, 19),
                new Token(TokenType.ADD_OPERATOR, '+' as char, 10, 21),
                new Token(TokenType.INT_CONST, 2, 10, 23),
                new Token(TokenType.RIGHT_PARENTHESES, ')' as char, 10, 24),
                new Token(TokenType.KW_AS, "as", 10, 26),
                new Token(TokenType.KW_STRING, "string", 10, 29),
                new Token(TokenType.SEMICOLON, ';' as char, 10, 35),
                new Token(TokenType.COMMENT, null, 10, 37),

                new Token(TokenType.IDENTIFIER, "print", 11, 5),
                new Token(TokenType.LEFT_PARENTHESES, '(' as char, 11, 10),
                new Token(TokenType.IDENTIFIER, "str", 11, 11),
                new Token(TokenType.RIGHT_PARENTHESES, ')' as char, 11, 14),
                new Token(TokenType.SEMICOLON, ';' as char, 11, 15),
                new Token(TokenType.COMMENT, null, 11, 17),

                new Token(TokenType.KW_RETURN, "return", 13, 5),
                new Token(TokenType.INT_CONST, 0, 13, 12),
                new Token(TokenType.SEMICOLON, ";" as char, 13, 13),
                new Token(TokenType.COMMENT, null, 13, 15),

                new Token(TokenType.RIGHT_CURLY_BRACKET, '}' as char, 14, 1),
                new Token(TokenType.EOF, null, 14, 2)
        ]
    }

    def 'Should act on backslash correctly'() {
        expect:
        tokenize(code) == tokens

        where:
        code << ["string quote = \"Albert Einstain said \\\"Reality is merely an illusion, albeit a very persistent one.\\\"\";",
                 "string hint = \"To type quotes use backslash \\\"\\\\\\\".\";"]
        tokens << [
                [
                        new Token(TokenType.KW_STRING, "string", 1, 1),
                        new Token(TokenType.IDENTIFIER, "quote", 1, 8),
                        new Token(TokenType.ASSIGNMENT, '=' as char, 1, 14),
                        new Token(TokenType.STRING_CONST, "Albert Einstain said \"Reality is merely an illusion, albeit a very persistent one.\"", 1, 16),
                        new Token(TokenType.SEMICOLON, ';' as char, 1, 103),
                        new Token(TokenType.EOF, null, 1, 104)
                ],
                [
                        new Token(TokenType.KW_STRING, "string", 1, 1),
                        new Token(TokenType.IDENTIFIER, "hint", 1, 8),
                        new Token(TokenType.ASSIGNMENT, '=' as char, 1, 13),
                        new Token(TokenType.STRING_CONST, "To type quotes use backslash \"\\\".", 1, 15),
                        new Token(TokenType.SEMICOLON, ';' as char, 1, 53),
                        new Token(TokenType.EOF, null, 1, 54)
                ],
        ]
    }

    def 'Should throw when string literal has not been closed'() {
        when:
        tokenize("\"abc\\")
        then:
        LexicalAnalyzerException e = thrown()
    }

    def 'Should throw when string literal break into a newline'() {
        when:
            tokenize("\"abc\ndef\"")
        then:
            LexicalAnalyzerException e = thrown()
    }

    def 'Should not throw if string contains newline character'() {
        when:
            tokenize("\"abc\\ndef\"");
        then:
            [
                    new Token(TokenType.STRING_CONST, "abc\\ndef", 1, 1),
                    new Token(TokenType.EOF, null, 1, 11)
            ]
    }
}