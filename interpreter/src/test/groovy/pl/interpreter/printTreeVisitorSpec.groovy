package pl.interpreter

import pl.interpreter.lexical_analyzer.LexicalAnalyzer
import pl.interpreter.parser.Parser
import pl.interpreter.parser.PrintTreeVisitor
import spock.lang.Specification

class printTreeVisitorSpec extends Specification{

    def parseProgram(String code) {
        var reader = new StringReader(code)
        var analyzer = new LexicalAnalyzer(reader);
        var parser = new Parser(analyzer);
        return parser.parseProgram();
    }

    def "Should print function declaration correctly"() {
        var program = parseProgram("int main(int a) { }")
        var writer = new StringWriter()
        (new PrintTreeVisitor(writer)).visit(program)

        expect:
            writer.toString() ==
"""
Program
|-FunctionDefinition <row: 1, col: 1>
  |-FunctionSignature <row: 1, col: 1>
    |-FunctionReturnType <row: 1, col: 1>
  |-FunctionParameters
    |-ParameterSignature <row: 1, col: 10>
  |-Block <row: 1, col: 17>
  |-Statements
"""
    }

}
