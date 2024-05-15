package pl.interpreter

import pl.interpreter.executor.StructureConstructor
import pl.interpreter.executor.StructureToFunctionMapper
import pl.interpreter.executor.ValueType
import pl.interpreter.parser.Parameter
import pl.interpreter.parser.ParameterType
import pl.interpreter.parser.StructureDefinition
import pl.interpreter.parser.VariableType
import spock.lang.Specification

class StructureToFunctionMapperSpec extends Specification {

    def "Should map structure definition correctly"() {
        var definition = new StructureDefinition(
                "Circle",
                List.of(
                    new Parameter(
                            new ParameterType(
                                    VariableType.USER_TYPE,
                                    "MetaData"
                            ),
                            "metaData",
                            null
                    ),
                    new Parameter(
                            new ParameterType(
                                    VariableType.FLOAT,
                                    null
                            ),
                            "r",
                            null
                    ),
                    new Parameter(
                            new ParameterType(
                                    VariableType.INT,
                                    null
                            ),
                            "x",
                            null
                    ),
                    new Parameter(
                            new ParameterType(
                                    VariableType.STRING,
                                    null
                            ),
                            "y",
                            null
                    ),
                ),
                null
        )
        var result = StructureToFunctionMapper.map(definition)

        expect:
        result.getReturnType() == new ValueType(ValueType.Type.USER_TYPE, "Circle")
        var sc = (result as StructureConstructor)
        sc.getExpectedParameterTypes() == List.of(
                new ValueType(ValueType.Type.USER_TYPE, "MetaData"),
                new ValueType(ValueType.Type.FLOAT),
                new ValueType(ValueType.Type.INT),
                new ValueType(ValueType.Type.STRING)
        )
        sc.getStructureName() == "Circle"
        sc.getFieldNames() == List.of(
                "metaData",
                "r",
                "x",
                "y"
        )
    }

}
