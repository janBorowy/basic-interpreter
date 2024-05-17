package pl.interpreter

import pl.interpreter.executor.Environment
import pl.interpreter.parser.Block
import pl.interpreter.parser.FunctionCall
import pl.interpreter.parser.FunctionDefinition
import pl.interpreter.parser.FunctionReturnType
import pl.interpreter.parser.FunctionReturnTypeEnum
import pl.interpreter.parser.Instruction
import pl.interpreter.parser.IntLiteral
import pl.interpreter.parser.Program
import pl.interpreter.parser.ReturnStatement
import pl.interpreter.parser.StringLiteral
import spock.lang.Specification

class BuiltInFunctionSpec extends Specification {

    def "Should print correctly"() {
        var writer = new StringWriter()
        var environment = new Environment(
                new Program(
                        Map.of(
                                "main",
                                new FunctionDefinition(
                                        new FunctionReturnType(FunctionReturnTypeEnum.INT, null),
                                        "main",
                                        List.of(),
                                        new Block(
                                                List.of(
                                                        new FunctionCall("print", List.of(new StringLiteral("Hello world!", null)), null) as Instruction,
                                                        new ReturnStatement(new IntLiteral(0, null), null)
                                                ),
                                                null
                                        ),
                                        null
                                )
                        ),
                        null
                ),
                writer
        )
        environment.runFunction("main", List.of())
        expect:
        writer.toString() == "Hello world!"
    }

    def "Should print in correct order"() {
        var writer = new StringWriter()
        var environment = new Environment(
                new Program(
                        Map.of(
                                "main",
                                new FunctionDefinition(
                                        new FunctionReturnType(FunctionReturnTypeEnum.INT, null),
                                        "main",
                                        List.of(),
                                        new Block(
                                                List.of(
                                                        new FunctionCall("print", List.of(new StringLiteral("Hello world!\n", null)), null) as Instruction,
                                                        new FunctionCall("print", List.of(new StringLiteral("Hello you!", null)), null) as Instruction,
                                                        new ReturnStatement(new IntLiteral(0, null), null)
                                                ),
                                                null
                                        ),
                                        null
                                )
                        ),
                        null
                ),
                writer
        )
        environment.runFunction("main", List.of())
        expect:
        writer.toString() == "Hello world!\nHello you!"
    }
}
