package pl.interpreter

import pl.interpreter.lexical_analyzer.LexicalAnalyzer
import pl.interpreter.parser.Parser
import pl.interpreter.parser.ast.Block
import pl.interpreter.parser.ast.FunctionDefinition
import pl.interpreter.parser.ast.FunctionReturnType
import pl.interpreter.parser.ast.FunctionReturnTypeEnum
import pl.interpreter.parser.ast.FunctionSignature
import pl.interpreter.parser.ast.ParameterSignature
import pl.interpreter.parser.ast.Program
import pl.interpreter.parser.ast.StructureDefinition
import pl.interpreter.parser.ast.VariableType
import pl.interpreter.parser.ast.VariantDefinition
import spock.lang.Specification

class ParserSpec extends Specification {

    def parseProgram(code) {
        var reader = new StringReader(code)
        var analyzer = new LexicalAnalyzer(reader);
        var parser = new Parser(analyzer);
        return parser.parseProgram();
    }

    def "Should parse function definitions"() {
        given:
            var program = parseProgram("int main() { }")
        expect:
            program == new Program(
                    List.of(
                            new FunctionDefinition(
                                new FunctionSignature(new FunctionReturnType(FunctionReturnTypeEnum.INT), "main"),
                                List.of(),
                                new Block(List.of())
                            )
                    )
            )
    }

    def "Should parse structure definitions"() {
        given:
        var program = parseProgram("struct User { string username; Hero hero; }")
        expect:
        program == new Program(
                List.of(
                        new StructureDefinition(
                                "User",
                                List.of(
                                        new ParameterSignature(VariableType.STRING, "username"),
                                        new ParameterSignature(VariableType.USER_TYPE, "hero", Optional.of("Hero"))
                                )
                        )
                )
        )
    }

    def "Should parse variant definitions"() {
        given:
        var program = parseProgram("variant Figure { Circle, Square }")
        expect:
        program == new Program(
                List.of(
                        new VariantDefinition(
                                "Figure",
                                List.of(
                                        "Circle",
                                        "Square"
                                )
                        )
                )
        )
    }

}
