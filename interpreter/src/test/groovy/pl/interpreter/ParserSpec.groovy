package pl.interpreter

import pl.interpreter.lexical_analyzer.LexicalAnalyzer
import pl.interpreter.parser.Parser

import pl.interpreter.parser.ast.ArithmeticCondition
import pl.interpreter.parser.ast.Block
import pl.interpreter.parser.ast.BooleanExpression
import pl.interpreter.parser.ast.Match
import pl.interpreter.parser.ast.MatchBranch
import pl.interpreter.parser.ast.Parentheses
import pl.interpreter.parser.ast.Expression
import pl.interpreter.parser.ast.FloatConst
import pl.interpreter.parser.ast.FunctionCall
import pl.interpreter.parser.ast.FunctionDefinition
import pl.interpreter.parser.ast.FunctionReturnType
import pl.interpreter.parser.ast.FunctionReturnTypeEnum
import pl.interpreter.parser.ast.FunctionSignature
import pl.interpreter.parser.ast.IdentifierStatement
import pl.interpreter.parser.ast.IdentifierWithValue
import pl.interpreter.parser.ast.If
import pl.interpreter.parser.ast.IntConst
import pl.interpreter.parser.ast.ParameterSignature
import pl.interpreter.parser.ast.PrimitiveInitialization
import pl.interpreter.parser.ast.Program
import pl.interpreter.parser.ast.Relation
import pl.interpreter.parser.ast.Return
import pl.interpreter.parser.ast.StringLiteral
import pl.interpreter.parser.ast.StructureDefinition
import pl.interpreter.parser.ast.Subcondition
import pl.interpreter.parser.ast.Term
import pl.interpreter.parser.ast.ValueAssignment
import pl.interpreter.parser.ast.VarInitialization
import pl.interpreter.parser.ast.VariableAssignment
import pl.interpreter.parser.ast.VariableType
import pl.interpreter.parser.ast.VariantDefinition
import pl.interpreter.parser.ast.While
import spock.lang.Specification

class ParserSpec extends Specification {

    def parseProgram(String code) {
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
                                new FunctionSignature(new FunctionReturnType(FunctionReturnTypeEnum.INT, 1, 1), "main", 1, 1),
                                List.of(),
                                new Block(List.of(), 1, 12),
                                1,
                                1
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
                                        new ParameterSignature(VariableType.STRING, "username", 1, 15),
                                        new ParameterSignature(VariableType.USER_TYPE, "hero", Optional.of("Hero"), 1, 32)
                                ),
                                1,
                                1
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
                                ),
                                1,
                                1
                        )
                )
        )
    }

    def "Should parse primitive initialization"() {
        var program = parseProgram("int main() { int a = 3; }")

        expect:
        program == new Program(
                List.of(
                        new FunctionDefinition(
                                new FunctionSignature(new FunctionReturnType(FunctionReturnTypeEnum.INT, 1, 1), "main", 1, 1),
                                List.of(),
                                new Block(List.of(
                                        new PrimitiveInitialization(VariableType.INT, "a",
                                                new Expression(
                                                        List.of(
                                                                new Term(
                                                                        List.of(
                                                                                new IntConst(3, 1, 22)
                                                                        ),
                                                                        List.of(),
                                                                        1, 22
                                                                )
                                                        ),
                                                        List.of(),
                                                        1, 22
                                                ),
                                                1, 14)
                                ), 1, 12),
                                1,
                                1
                        )
                )
        )
    }

    def "Should parse value assignment"() {
        var program = parseProgram("int main() { a = 1; }")

        expect:
        program == new Program(
                List.of(
                        new FunctionDefinition(
                                new FunctionSignature(new FunctionReturnType(FunctionReturnTypeEnum.INT, 1, 1), "main", 1, 1),
                                List.of(),
                                new Block(List.of(
                                        new IdentifierStatement("a", new ValueAssignment(
                                                new Expression(
                                                        List.of(
                                                                new Term(
                                                                        List.of(
                                                                                new IntConst(1, 1, 18)
                                                                        ),
                                                                        List.of(),
                                                                        1, 18
                                                                )
                                                        ),
                                                        List.of(),
                                                        1, 18
                                                ),
                                                1, 16
                                        ), 1, 14)
                                ), 1, 12),
                                1,
                                1
                        )
                )
        )
    }

    def "Should parse function call"() {
        var program = parseProgram("int main() { b(3); }")

        expect:
        program == new Program(
                List.of(
                        new FunctionDefinition(
                                new FunctionSignature(new FunctionReturnType(FunctionReturnTypeEnum.INT, 1, 1), "main", 1, 1),
                                List.of(),
                                new Block(List.of(
                                        new IdentifierStatement("b", new FunctionCall(List.of(
                                                new Expression(
                                                        List.of(
                                                                new Term(
                                                                        List.of(
                                                                                new IntConst(3, 1, 16)
                                                                        ),
                                                                        List.of(),
                                                                        1, 16
                                                                )
                                                        ),
                                                        List.of(),
                                                        1, 16
                                                )
                                        ), 1, 15), 1, 14)
                                ), 1, 12),
                                1,
                                1
                        )
                )
        )
    }

    def "Should parse user type initialization"() {
        var program = parseProgram("int main() { Circle c = Circle(1.5); }")

        expect:
        program == new Program(
                List.of(
                        new FunctionDefinition(
                                new FunctionSignature(new FunctionReturnType(FunctionReturnTypeEnum.INT, 1, 1), "main", 1, 1),
                                List.of(),
                                new Block(List.of(
                                        new IdentifierStatement("Circle",
                                                new VariableAssignment("c",
                                                        new Expression(
                                                                List.of(new Term(
                                                                        List.of(
                                                                                new IdentifierWithValue("Circle",
                                                                                        Optional.of(new FunctionCall(
                                                                                                List.of(new Expression(
                                                                                                        List.of(
                                                                                                                new Term(
                                                                                                                        List.of(
                                                                                                                                new FloatConst(1.5, 1, 32)
                                                                                                                        ),
                                                                                                                        List.of(),
                                                                                                                        1, 32
                                                                                                                )
                                                                                                        ),
                                                                                                        List.of(),
                                                                                                        1, 32
                                                                                                )),
                                                                                                1, 31
                                                                                        )),
                                                                                        1, 25)
                                                                        ),
                                                                        List.of(),
                                                                        1,
                                                                        25
                                                                )
                                                                ),
                                                                List.of(),
                                                                1, 25
                                                        ),
                                                        1, 21
                                                ),
                                                1, 14)
                                ), 1, 12),
                                1,
                                1
                        )
                )
        )
    }

    def "Should parse var initialization"() {
        var program = parseProgram("int main() { var int i = 5; }")

        expect:
        program == new Program(
                List.of(
                        new FunctionDefinition(
                                new FunctionSignature(new FunctionReturnType(FunctionReturnTypeEnum.INT, 1, 1), "main", 1, 1),
                                List.of(),
                                new Block(List.of(
                                        new VarInitialization(
                                                new PrimitiveInitialization(
                                                        VariableType.INT,
                                                        "i",
                                                        new Expression(
                                                                List.of(
                                                                        new Term(
                                                                                List.of(
                                                                                        new IntConst(5, 1, 26)
                                                                                ),
                                                                                List.of(),
                                                                                1, 26
                                                                        )
                                                                ),
                                                                List.of(),
                                                                1, 26
                                                        ),
                                                        1, 18
                                                ),
                                                1, 14)
                                ), 1, 12),
                                1,
                                1
                        )
                )
        )
    }

    def "Should parse return statement"() {
        var program = parseProgram("int main() { return 0; }")

        expect:
        program == new Program(
                List.of(
                        new FunctionDefinition(
                                new FunctionSignature(new FunctionReturnType(FunctionReturnTypeEnum.INT, 1, 1), "main", 1, 1),
                                List.of(),
                                new Block(List.of(
                                        new Return(
                                                Optional.of(new Expression(
                                                        List.of(
                                                                new Term(
                                                                        List.of(
                                                                                new IntConst(0, 1, 21)
                                                                        ),
                                                                        List.of(),
                                                                        1, 21
                                                                )
                                                        ),
                                                        List.of(),
                                                        1, 21
                                                )),
                                                1, 14
                                        )
                                ), 1, 12),
                                1,
                                1
                        )
                )
        )
    }

    def "Should parse if else statement"() {
        var program = parseProgram(
                "int main() { if(a > b) { print(\"hello world\"); } else print(\"don't\"); }")
        expect:


        program == new Program(
                List.of(
                        new FunctionDefinition(
                                new FunctionSignature(new FunctionReturnType(FunctionReturnTypeEnum.INT, 1, 1), "main", 1, 1),
                                List.of(),
                                new Block(List.of(
                                        new If(
                                                new Parentheses(
                                                        List.of(
                                                                new Subcondition(
                                                                        List.of(
                                                                                new BooleanExpression(
                                                                                        new Relation(
                                                                                                new Expression(
                                                                                                        List.of(
                                                                                                                new Term(
                                                                                                                        List.of(
                                                                                                                                new IdentifierWithValue('a', Optional.empty(), 1, 17)
                                                                                                                        ),
                                                                                                                        List.of(),
                                                                                                                        1, 17
                                                                                                                )
                                                                                                        ),
                                                                                                        List.of(),
                                                                                                        1, 17
                                                                                                ),
                                                                                                Optional.of(ArithmeticCondition.GREATER_THAN),
                                                                                                Optional.of(new Expression(
                                                                                                        List.of(
                                                                                                                new Term(
                                                                                                                        List.of(
                                                                                                                                new IdentifierWithValue('b', Optional.empty(), 1, 21)
                                                                                                                        ),
                                                                                                                        List.of(),
                                                                                                                        1, 21
                                                                                                                )
                                                                                                        ),
                                                                                                        List.of(),
                                                                                                        1, 21
                                                                                                )),
                                                                                                1, 17
                                                                                        ),
                                                                                        false,
                                                                                        1, 17
                                                                                )
                                                                        ),
                                                                        1, 17
                                                                )
                                                        ),
                                                        1, 17
                                                ),
                                                new Block(
                                                        List.of(
                                                                new IdentifierStatement("print", new FunctionCall(
                                                                        List.of(new StringLiteral("hello world", 1, 32)),
                                                                        1, 31
                                                                ), 1, 26)
                                                        ),
                                                        1, 24
                                                ),
                                                Optional.of(new IdentifierStatement("print", new FunctionCall(
                                                        List.of(new StringLiteral("don't", 1, 61)),
                                                        1, 60
                                                ), 1, 55)),
                                                1, 14
                                        )
                                ), 1, 12),
                                1,
                                1
                        )
                )
        )
    }

    def "Should parse while statement"() {
        var program = parseProgram("int main() { while(a == b) { print(\"hello\"); } }")

        expect:
        program == new Program(
                List.of(
                        new FunctionDefinition(
                                new FunctionSignature(new FunctionReturnType(FunctionReturnTypeEnum.INT, 1, 1), "main", 1, 1),
                                List.of(),
                                new Block(List.of(
                                        new While(
                                                new Parentheses(
                                                        List.of(
                                                                new Subcondition(List.of(
                                                                        new BooleanExpression(
                                                                                new Relation(
                                                                                        new Expression(
                                                                                                List.of(
                                                                                                        new Term(
                                                                                                                List.of(
                                                                                                                        new IdentifierWithValue('a', Optional.empty(), 1, 20)
                                                                                                                ),
                                                                                                                List.of(),
                                                                                                                1, 20
                                                                                                        )
                                                                                                ),
                                                                                                List.of(),
                                                                                                1, 20
                                                                                        ),
                                                                                        Optional.of(ArithmeticCondition.EQUAL),
                                                                                        Optional.of(
                                                                                                new Expression(
                                                                                                        List.of(
                                                                                                                new Term(
                                                                                                                        List.of(
                                                                                                                                new IdentifierWithValue('b', Optional.empty(), 1, 25)
                                                                                                                        ),
                                                                                                                        List.of(),
                                                                                                                        1, 25
                                                                                                                )
                                                                                                        ),
                                                                                                        List.of(),
                                                                                                        1, 25
                                                                                                )
                                                                                        ), 1, 20
                                                                                ),
                                                                                false, 1, 20
                                                                        )), 1, 20
                                                                )
                                                        ), 1, 20
                                                ),
                                                new Block(List.of(
                                                        new IdentifierStatement("print",
                                                                new FunctionCall(List.of(
                                                                        new StringLiteral("hello", 1, 36)
                                                                ),
                                                                        1, 35),
                                                                1, 30)
                                                ), 1, 28),
                                                1, 14
                                        )
                                ), 1, 12),
                                1,
                                1
                        )
                )
        )
    }

    def "Should parse match statement"() {
        var program = parseProgram(
"""int main() {
    match (point) {
        FloatPoint floatPoint -> print("float");
        IntPoint intPoint -> print("int");
    }
}
""")
        expect:
            program == new Program(
                    List.of(
                            new FunctionDefinition(
                                    new FunctionSignature(new FunctionReturnType(FunctionReturnTypeEnum.INT, 1, 1), "main", 1, 1),
                                    List.of(),
                                    new Block(List.of(
                                        new Match(new IdentifierWithValue(("point"), Optional.empty(), 2, 12),
                                                List.of(
                                                        new MatchBranch(
                                                                "FloatPoint", "floatPoint",
                                                                new IdentifierStatement("print",
                                                                new FunctionCall(List.of(
                                                                        new StringLiteral("float", 3, 40)),
                                                                        3, 39), 3, 34),
                                                                3, 9
                                                        ),
                                                        new MatchBranch(
                                                                "IntPoint", "intPoint",
                                                                new IdentifierStatement("print",
                                                                        new FunctionCall(List.of(
                                                                                new StringLiteral("int", 4, 36)),
                                                                                4, 35), 4, 30),
                                                                4, 9
                                                        )
                                                ),
                                                2, 5
                                        )
                                    ), 1, 12),
                                    1,
                                    1
                            )
                    )
            )
        }

}
