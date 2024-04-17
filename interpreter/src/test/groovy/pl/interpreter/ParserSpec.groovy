package pl.interpreter

import pl.interpreter.lexical_analyzer.LexicalAnalyzer
import pl.interpreter.parser.Parser
import pl.interpreter.parser.ast.Block
import pl.interpreter.parser.ast.Expression
import pl.interpreter.parser.ast.Factor
import pl.interpreter.parser.ast.FloatConst
import pl.interpreter.parser.ast.FunctionArguments
import pl.interpreter.parser.ast.FunctionDefinition
import pl.interpreter.parser.ast.FunctionReturnType
import pl.interpreter.parser.ast.FunctionReturnTypeEnum
import pl.interpreter.parser.ast.FunctionSignature
import pl.interpreter.parser.ast.IdentifierStatement
import pl.interpreter.parser.ast.IdentifierWithValue
import pl.interpreter.parser.ast.Instruction
import pl.interpreter.parser.ast.IntConst
import pl.interpreter.parser.ast.ParameterSignature
import pl.interpreter.parser.ast.PrimitiveInitialization
import pl.interpreter.parser.ast.Program
import pl.interpreter.parser.ast.Return
import pl.interpreter.parser.ast.StructureDefinition
import pl.interpreter.parser.ast.Term
import pl.interpreter.parser.ast.ValueAssignment
import pl.interpreter.parser.ast.VarInitialization
import pl.interpreter.parser.ast.VariableAssignment
import pl.interpreter.parser.ast.VariableType
import pl.interpreter.parser.ast.VariantDefinition
import spock.lang.Specification

class ParserSpec extends Specification {

    def parseProgram(String code) {
        var reader = new StringReader(code)
        var analyzer = new LexicalAnalyzer(reader);
        var parser = new Parser(analyzer);
        return parser.parseProgram();
    }

    def createProgramWithMain(List<Instruction> instructions) {
        return new Program(
                List.of(
                        new FunctionDefinition(
                                new FunctionSignature(new FunctionReturnType(FunctionReturnTypeEnum.INT), "main"),
                                List.of(),
                                new Block(instructions)
                        )
                )
        )
    }

    def createSingleValueWithinExpression(Factor value) {
        return new Expression(List.of(
                new Term(List.of(
                        value
                ), List.of())
        ), List.of())
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

    def "Should parse single statements"() {
        given:
            var program = parseProgram(
                    """
                                int main() {
                                    int a = 3;
                                    a = 1;
                                    b(3);
                                    Circle c = Circle(1.5);
                                    var int i = 5;
                                    return 0;
                                }
                              """)
        expect:
            program == createProgramWithMain(
                    List.of(
                            new PrimitiveInitialization(VariableType.INT, "a", createSingleValueWithinExpression(new IntConst(3))),
                            new IdentifierStatement("a", new ValueAssignment(createSingleValueWithinExpression(new IntConst(1)))),
                            new IdentifierStatement("b", new FunctionArguments(List.of(createSingleValueWithinExpression(new IntConst(3))))),
                            new IdentifierStatement("Circle", new VariableAssignment("c",
                                    createSingleValueWithinExpression(new IdentifierWithValue("Circle",
                                    Optional.of(new FunctionArguments(List.of(createSingleValueWithinExpression(new FloatConst(1.5)))))))
                            )),
                            new VarInitialization(new PrimitiveInitialization(VariableType.INT, "i", createSingleValueWithinExpression(new IntConst(5)))),
                            new Return(Optional.of(createSingleValueWithinExpression(new IntConst(0))))
                    )
            )
    }

}
