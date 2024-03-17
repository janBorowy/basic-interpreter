package pl.interpreter

import pl.interpreter.LexicalAnalyzer

import spock.lang.Specification
import spock.lang.Subject

class LexicalAnalyzerSpec extends Specification {

    @Subject
    def static lexicalAnalyzer = new LexicalAnalyzer();

    def 'must return correct identifier'() {
        expect:
        lexicalAnalyzer.getToken() == token

        where:
        token << ["a"]
    }
}