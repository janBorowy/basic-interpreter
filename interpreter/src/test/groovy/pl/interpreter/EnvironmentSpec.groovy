package pl.interpreter

import pl.interpreter.executor.Environment
import pl.interpreter.lexical_analyzer.LexicalAnalyzer
import pl.interpreter.parser.ProgramParser
import pl.interpreter.parser.TokenManager
import spock.lang.Specification

class EnvironmentSpec extends Specification {

    def getProgramParser(code) {
        var reader = new StringReader(code);
        var lexer = new LexicalAnalyzer(reader);
        var tokenManager = new TokenManager(lexer);
        return new ProgramParser(tokenManager);
    }

    def getTree(code) {
        var parser = getProgramParser(code)
        var statement = parser.parse()
        return statement;
    }

    def "Should initialize correctly"() {
        expect:
        new Environment(getTree(""))
        new Environment(getTree("int main() { }"))
        new Environment(getTree("struct Point { int x, int y } int main() { }"))
    }

}
