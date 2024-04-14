package pl.interpreter

import pl.interpreter.lexical_analyzer.LexicalAnalyzer
import pl.interpreter.parser.Parser
import pl.interpreter.parser.ast.FunctionDefinition
import pl.interpreter.parser.ast.FunctionParameters
import pl.interpreter.parser.ast.FunctionSignature
import pl.interpreter.parser.ast.ParameterSignature
import pl.interpreter.parser.ast.Program
import pl.interpreter.parser.ast.VoidType
import spock.lang.Specification

class ParserSpec extends Specification{

    def "Should parse function definition correctly"() {
        given:
            var reader = new StringReader("void main()")
            var analyzer = new LexicalAnalyzer(reader);
            var parser = new Parser(analyzer);
            var program = parser.parseProgram();
        expect:
            program == new Program(List.of(
                    new FunctionDefinition(
                            new FunctionSignature(new VoidType(), "main"),
                            new FunctionParameters(null)
                    )
            ), List.of(), List.of())
    }
}
