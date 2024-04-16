package pl.interpreter

import pl.interpreter.lexical_analyzer.LexicalAnalyzer
import pl.interpreter.parser.Parser
import spock.lang.Specification

class ParserSpec extends Specification {

    def parseProgram(code) {
        var reader = new StringReader(code)
        var analyzer = new LexicalAnalyzer(reader);
        var parser = new Parser(analyzer);
        return parser.parseProgram();
    }

}
