package pl.interpreter

import pl.interpreter.lexical_analyzer.LexicalAnalyzer
import pl.interpreter.parser.Parser
import pl.interpreter.parser.ast.AdditiveOperator
import pl.interpreter.parser.ast.Block
import pl.interpreter.parser.ast.BoolConst
import pl.interpreter.parser.ast.Expression
import pl.interpreter.parser.ast.Factor
import pl.interpreter.parser.ast.FloatConst
import pl.interpreter.parser.ast.FunctionDefinition
import pl.interpreter.parser.ast.FunctionParameters
import pl.interpreter.parser.ast.FunctionSignature
import pl.interpreter.parser.ast.Initialization
import pl.interpreter.parser.ast.InitializationSignature
import pl.interpreter.parser.ast.IntConst
import pl.interpreter.parser.ast.Operator
import pl.interpreter.parser.ast.ParameterSignature
import pl.interpreter.parser.ast.Program
import pl.interpreter.parser.ast.SingleStatement
import pl.interpreter.parser.ast.StringConst
import pl.interpreter.parser.ast.Term
import pl.interpreter.parser.ast.UserType
import pl.interpreter.parser.ast.Value
import pl.interpreter.parser.ast.VariableType
import pl.interpreter.parser.ast.VariableTypeEnum
import pl.interpreter.parser.ast.VoidType
import spock.lang.Specification

class ParserSpec extends Specification {

    def parseProgram(code) {
        var reader = new StringReader(code)
        var analyzer = new LexicalAnalyzer(reader);
        var parser = new Parser(analyzer);
        return parser.parseProgram();
    }

    def "Should parse empty function definition correctly"() {
        expect:
        parseProgram(code) == p
        where:
        code                                         | p
        "int main() {}"                              | new Program(List.of(
                new FunctionDefinition(
                        new FunctionSignature(new VariableType(VariableTypeEnum.INT), "main"),
                        new FunctionParameters(List.of()),
                        new Block(List.of()))
        ), List.of(), List.of())
        "int add(int a, int b) {}"                   | new Program(List.of(
                new FunctionDefinition(
                        new FunctionSignature(new VariableType(VariableTypeEnum.INT), "add"),
                        new FunctionParameters(List.of(
                                new ParameterSignature(new VariableType(VariableTypeEnum.INT), "a"),
                                new ParameterSignature(new VariableType(VariableTypeEnum.INT), "b"))),
                        new Block(List.of())
                )), List.of(), List.of())
        "Vector dot(Vector first, Vector second) {}" | new Program(List.of(
                new FunctionDefinition(
                        new FunctionSignature(new UserType("Vector"), "dot"),
                        new FunctionParameters(List.of(
                                new ParameterSignature(new UserType("Vector"), "first"),
                                new ParameterSignature(new UserType("Vector"), "second")
                        )),
                        new Block(List.of())
                )), List.of(), List.of())
    }

    def "Should parse functions definitions with initializations correctly"() {
        expect:
        parseProgram(code) == p
        where:
        code                                                                                                           | p
        "int main() { var int a = 0; float hello = 1.5 + 3.5; string HELLO = \"Hello World\";}" | new Program(List.of(
                new FunctionDefinition(
                        new FunctionSignature(new VariableType(VariableTypeEnum.INT), "main"),
                        new FunctionParameters(List.of()),
                        new Block(List.of(
                                new SingleStatement(
                                        new Initialization(
                                                new InitializationSignature(
                                                        true,
                                                        new VariableType(VariableTypeEnum.INT),
                                                        "a"
                                                ),
                                                new Value(
                                                        new Expression(List.of(new Term(List.of(
                                                                new Factor(
                                                                        new IntConst(0)
                                                                )
                                                        ), List.of()))
                                                                , List.of())
                                                )
                                        )
                                ),
                                new SingleStatement(
                                        new Initialization(
                                                new InitializationSignature(
                                                        false,
                                                        new VariableType(VariableTypeEnum.FLOAT),
                                                        "hello"
                                                ),
                                                new Value(
                                                        new Expression(
                                                                List.of(
                                                                        new Term(List.of(
                                                                                new Factor(
                                                                                            new FloatConst(
                                                                                                    1.5
                                                                                            )
                                                                                )
                                                                        ), List.of()),
                                                                        new Term(List.of(
                                                                                new Factor(
                                                                                            new FloatConst(
                                                                                                    3.5
                                                                                            )
                                                                                )
                                                                        ), List.of())
                                                                ),
                                                                List.of(
                                                                        new AdditiveOperator(Operator.ADD)
                                                                )
                                                        )
                                                )
                                        )
                                ),
                                new SingleStatement(
                                        new Initialization(
                                                new InitializationSignature(
                                                        false,
                                                        new VariableType(VariableTypeEnum.STRING),
                                                        "HELLO"
                                                ),
                                                new Value(
                                                        new Expression(List.of(new Term(List.of(
                                                                new Factor(
                                                                        new StringConst("Hello World")
                                                                )
                                                        ), List.of()))
                                                        , List.of())
                                                )
                                        )
                                )
                        ))
                )
        ), List.of(), List.of())
    }

}
