package pl.interpreter

import pl.interpreter.lexicalAnalyzer.LexicalAnalyzer
import pl.interpreter.lexicalAnalyzer.NumberTokenizationException
import spock.lang.Specification
import java.nio.file.Path

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

    def 'Should tokenize singly tokens correctly'() {
        given:
        def code = "+ - * / % < > ! . = ( ) { } ;"

        expect:
        tokenize(code) == [
                new Token(TokenType.ADDITIVE_OPERATOR, '+' as char, 1, 1),
                new Token(TokenType.ADDITIVE_OPERATOR, '-' as char, 1, 3),
                new Token(TokenType.MULTIPLICATIVE_OPERATOR, '*' as char, 1, 5),
                new Token(TokenType.MULTIPLICATIVE_OPERATOR, '/' as char, 1, 7),
                new Token(TokenType.MULTIPLICATIVE_OPERATOR, '%' as char, 1, 9),
                new Token(TokenType.RELATIONAL_OPERATOR, '<' as char, 1, 11),
                new Token(TokenType.RELATIONAL_OPERATOR, '>' as char, 1, 13),
                new Token(TokenType.RELATIONAL_OPERATOR, '!' as char, 1, 15),
                new Token(TokenType.COMMA, '.' as char, 1, 17),
                new Token(TokenType.ASSIGNMENT, '=' as char, 1, 19),
                new Token(TokenType.LEFT_PARENTHESES, '(' as char, 1, 21),
                new Token(TokenType.RIGHT_PARENTHESES, ')' as char, 1, 23),
                new Token(TokenType.LEFT_CURLY_BRACKET, '{' as char, 1 , 25),
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
                new Token(TokenType.RELATIONAL_OPERATOR, "==", 1, 1),
                new Token(TokenType.RELATIONAL_OPERATOR, "!=", 1, 4),
                new Token(TokenType.RELATIONAL_OPERATOR, "<=", 1, 7),
                new Token(TokenType.RELATIONAL_OPERATOR, ">=", 1, 10),
                new Token(TokenType.ARROW, "->", 1, 13),
                new Token(TokenType.EOF, null, 1, 15)
        ]
    }

    def 'Should show correct row, col values'() {
        given:
        def code = "== !=\n >= + \n   !\n--  \n"
        expect:
        tokenize(code) == [
                new Token(TokenType.RELATIONAL_OPERATOR, "==", 1, 1),
                new Token(TokenType.RELATIONAL_OPERATOR, "!=", 1, 4),
                new Token(TokenType.RELATIONAL_OPERATOR, ">=", 2, 2),
                new Token(TokenType.ADDITIVE_OPERATOR, '+' as char, 2, 5),
                new Token(TokenType.RELATIONAL_OPERATOR, '!' as char, 3, 4),
                new Token(TokenType.ADDITIVE_OPERATOR, '-' as char, 4, 1),
                new Token(TokenType.ADDITIVE_OPERATOR, '-' as char, 4, 2),
                new Token(TokenType.EOF, null, 5, 1)
        ]
    }

    def 'Should read number tokens correctly'() {
        given:
        def code = "1 1.5 21.37"
        expect:
        tokenize(code) == [
                new Token(TokenType.CONSTANT, Integer.valueOf(1), 1, 1),
                new Token(TokenType.CONSTANT, Float.valueOf(1.5), 1, 3),
                new Token(TokenType.CONSTANT, Float.valueOf(21.37), 1, 7),
                new Token(TokenType.EOF, null, 1, 12)
        ]
    }

    def 'Should throw number tokenization exception when number was not ended correctly'() {
        given:
        def code = "1. 1"
        when:
        tokenize(code)
        then:
        NumberTokenizationException e = thrown()
    }

    def 'Should tokenize string literal correctly'() {
        given:
        def code = "\"hello world\""

        expect:
        tokenize(code) == [
                new Token(TokenType.CONSTANT, "hello world", 1, 1),
                new Token(TokenType.EOF, null, 1, 14)
        ]
    }

    def 'Should tokenize multiple string literals correctly'() {
        given:
        def code = "\"   hello   \"\n\"world hello\""
        expect:
        tokenize(code) == [
                new Token(TokenType.CONSTANT, "   hello   ", 1, 1),
                new Token(TokenType.CONSTANT, "world hello", 2, 1),
                new Token(TokenType.EOF, null, 2, 14)
        ]
    }
//    def 'Should tokenize source_code_1 correctly'() {
//        given:
//        def reader = Files.newBufferedReader(sourceCode1)
//        def lexicalAnalyzer = new LexicalAnalyzer(reader)
//        def tokens = getAllTokens(lexicalAnalyzer)
//        expect:
//        tokens == [
//                new Token(TokenType.KEYWORD, "int", 1, 1),
//                new Token(TokenType.IDENTIFIER, "main", 1, 5),
//                new Token(TokenType.LEFT_PARENTHESES, null, 1, 9),
//                new Token(TokenType.RIGHT_PARENTHESES, null, 1, 10),
//                new Token(TokenType.LEFT_CURLY_BRACKET, null, 1, 12),
//                new Token(TokenType.EOF, null, 1, 13)
//        ]
//    }
}