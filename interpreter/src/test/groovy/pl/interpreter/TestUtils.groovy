package pl.interpreter

import pl.interpreter.executor.Environment
import pl.interpreter.lexical_analyzer.LexicalAnalyzer
import pl.interpreter.parser.ProgramParser
import pl.interpreter.parser.TokenManager

class TestUtils {

    var static final float DELTA = 0.01f;

    static boolean isClose(float a, float b) {
        return Math.abs(a - b) <= DELTA
    }

    static Environment prepareEnvironmentWithCode(final String code) {
        var final reader = new StringReader(code)
        var final lexer = new LexicalAnalyzer(reader)
        var final tokenManager = new TokenManager(lexer)
        var final parser = new ProgramParser(tokenManager)
        var final program = parser.parse()

        return new Environment(program)
    }
}
