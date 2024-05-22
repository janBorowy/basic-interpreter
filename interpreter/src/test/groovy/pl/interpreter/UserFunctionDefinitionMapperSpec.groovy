package pl.interpreter

import pl.interpreter.executor.FunctionParameter
import pl.interpreter.executor.UserFunction
import pl.interpreter.executor.UserFunctionDefinitionMapper
import pl.interpreter.executor.ValueType
import pl.interpreter.parser.Block
import pl.interpreter.parser.FunctionCall
import pl.interpreter.parser.FunctionDefinition
import pl.interpreter.parser.FunctionReturnType
import pl.interpreter.parser.FunctionReturnTypeEnum
import pl.interpreter.parser.AstFunctionParameter
import pl.interpreter.parser.ParameterType
import pl.interpreter.parser.StringLiteral
import pl.interpreter.parser.VariableType
import spock.lang.Specification

class UserFunctionDefinitionMapperSpec extends Specification {

    def "Should map function definition correctly"() {
        var block = new Block(
                List.of(
                        new FunctionCall(
                                "print",
                                List.of(new StringLiteral("hello world", null)),
                                null
                        )
                ),
                null
        )
        var definition = new FunctionDefinition(
                new FunctionReturnType(FunctionReturnTypeEnum.INT, null),
                "main",
                List.of(new AstFunctionParameter(new ParameterType(VariableType.INT, null), "argc", false, null)),
                block,
                null
        )
        var function = UserFunctionDefinitionMapper.map(definition)

        expect:
        function as UserFunction
        function.getReturnType() == new ValueType(ValueType.Type.INT)
        function.getParameters()[0] == new FunctionParameter("argc", new ValueType(ValueType.Type.INT), false)
        function.getBlock() == block
    }
}
