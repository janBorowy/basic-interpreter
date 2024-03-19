package pl.interpreter

import pl.interpreter.LexicalAnalyzer

import spock.lang.Specification
import spock.lang.Subject

import java.nio.file.Files
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
        } while(token.type() != TokenType.EOF)
        return tokens;
    }

    def 'Should tokenize singly tokens correctly'() {
        given:
        def code = "+ - * / % < > !"
        def reader = new StringReader(code)
        def lexicalAnalyzer = new LexicalAnalyzer(reader)
        
        when:
        def tokens = getAllTokens(lexicalAnalyzer)

        then:
        tokens.toString() == [
                new Token(TokenType.ADDITIVE_OPERATOR, '+', 1, 1),
                new Token(TokenType.ADDITIVE_OPERATOR, '-', 1, 3),
                new Token(TokenType.MULTIPLICATIVE_OPERATOR, '*', 1, 5),
                new Token(TokenType.MULTIPLICATIVE_OPERATOR, '/', 1, 7),
                new Token(TokenType.MULTIPLICATIVE_OPERATOR, '%', 1, 9),
                new Token(TokenType.RELATIONAL_OPERATOR, '<', 1, 11),
                new Token(TokenType.RELATIONAL_OPERATOR, '>', 1, 13),
                new Token(TokenType.RELATIONAL_OPERATOR, '!', 1, 15),
                new Token(TokenType.EOF, null, 1, 16)
        ].toString()
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