package pl.interpreter

import pl.interpreter.executor.BooleanValue
import pl.interpreter.executor.CallContext
import pl.interpreter.executor.ExpressionEvaluatingVisitor
import pl.interpreter.executor.FloatValue
import pl.interpreter.executor.IntValue
import pl.interpreter.executor.StringValue
import pl.interpreter.executor.exceptions.ExpressionEvaluationException
import pl.interpreter.executor.exceptions.InvalidValueTypeException
import pl.interpreter.lexical_analyzer.LexicalAnalyzer
import pl.interpreter.parser.AdditionOperator
import pl.interpreter.parser.BooleanLiteral
import pl.interpreter.parser.Value
import pl.interpreter.parser.ExpressionParser
import pl.interpreter.parser.FloatLiteral
import pl.interpreter.parser.IntLiteral
import pl.interpreter.parser.Multiplication
import pl.interpreter.parser.MultiplicationOperator
import pl.interpreter.parser.StringLiteral
import pl.interpreter.parser.Sum
import pl.interpreter.parser.TokenManager
import spock.lang.Specification

class ExpressionEvaluatingVisitorSpec extends Specification {

    def getTestingContext() {
        var context = new CallContext(new ArrayList<>())
        context.setVariableForClosestScope("a", new IntValue(1))
        context.setVariableForClosestScope("b", new FloatValue(1.5f))
        context.setVariableForClosestScope("c", new StringValue("abc"))
        context.setVariableForClosestScope("d", new BooleanValue(true))
        return context
    }

    def evaluateExpression(Value expression) {
        var visitor = new ExpressionEvaluatingVisitor(getTestingContext())
        visitor.visit(expression)
        return visitor.getValue()
    }

    def evaluateExpression(String code) {
        var analyzer = new LexicalAnalyzer(new StringReader(code))
        var tokenManager = new TokenManager(analyzer)
        var tree = new ExpressionParser(tokenManager)
        return evaluateExpression(tree.parseExpression().get())
    }

    def "Should evaluate integer literals"() {
        var result = evaluateExpression(new IntLiteral(1, null))
        expect:
        result in IntValue
        (result as IntValue).getValue() == 1
    }

    def "Should evaluate float literals"() {
        var result = evaluateExpression(new FloatLiteral(1.5, null))
        expect:
        result in FloatValue
        (result as FloatValue).getValue() == 1.5f
    }

    def "Should evaluate boolean literals"() {
        var result = evaluateExpression(new BooleanLiteral(true, null))
        expect:
        result in BooleanValue
        (result as BooleanValue).isTruthy()
    }

    def "Should evaluate string literals"() {
        var result = evaluateExpression(new StringLiteral('abc', null))
        expect:
        result in StringValue
        (result as StringValue).getValue() == 'abc'
    }

    def "Should evaluate int plus sum correctly"() {
        var result = evaluateExpression(
                new Sum(
                        new IntLiteral(2, null),
                        AdditionOperator.PLUS,
                        new IntLiteral(2, null),
                        null
                )
        )
        expect:
        result in IntValue
        (result as IntValue).getValue() == 4
    }

    def "Should evaluate int minus sum correctly"() {
        var result = evaluateExpression(
                new Sum(
                        new IntLiteral(2, null),
                        AdditionOperator.MINUS,
                        new IntLiteral(5, null),
                        null
                )
        )
        expect:
        result in IntValue
        (result as IntValue).getValue() == -3
    }

    def "Should evaluate float plus sum correctly"() {
        var result = evaluateExpression(
                new Sum(
                        new FloatLiteral(2.5, null),
                        AdditionOperator.PLUS,
                        new FloatLiteral(2.5, null),
                        null
                )
        )
        expect:
        result in FloatValue
        TestUtils.isClose((result as FloatValue).getValue(), 5.0f)
    }

    def "Should evaluate float minus sum correctly"() {
        var result = evaluateExpression(
                new Sum(
                        new FloatLiteral(2.5, null),
                        AdditionOperator.MINUS,
                        new FloatLiteral(2.0, null),
                        null
                )
        )
        expect:
        result in FloatValue
        TestUtils.isClose((result as FloatValue).getValue(), 0.5f)
    }

    def "Should concat strings"() {
        var result = evaluateExpression(
                new Sum(
                        new StringLiteral("ab", null),
                        AdditionOperator.PLUS,
                        new StringLiteral("cd", null),
                        null
                )
        )
        expect:
        result in StringValue
        (result as StringValue).getValue() == "abcd"
    }

    def "Should throw when subtracting strings"() {
        when:
        var result = evaluateExpression(
                new Sum(
                        new StringLiteral("ab", null),
                        AdditionOperator.MINUS,
                        new StringLiteral("cd", null),
                        null
                )
        )
        then:
        ExpressionEvaluationException e = thrown()
    }

    def "Should throw when summing boolean values"() {
        when:
        var result = evaluateExpression(
                new Sum(
                        new BooleanLiteral(true, null),
                        AdditionOperator.MINUS,
                        new FloatLiteral(0.1f, null),
                        null
                )
        )
        then:
        InvalidValueTypeException e = thrown()
    }

    def "Should throw when summing boolean value"() {
        when:
        var result = evaluateExpression(
                new Sum(
                        new FloatLiteral(0.1f, null),
                        AdditionOperator.MINUS,
                        new BooleanLiteral(true, null),
                        null
                )
        )
        then:
        InvalidValueTypeException e = thrown()
    }

    def "Should multiply integers correctly"() {
        var result = evaluateExpression(
                new Multiplication(
                        new IntLiteral(2, null),
                        MultiplicationOperator.MULTIPLY,
                        new IntLiteral(2, null),
                        null
                )
        )
        expect:
        result in IntValue
        (result as IntValue).getValue() == 4
    }

    def "Should multiply float correctly"() {
        var result = evaluateExpression(
                new Multiplication(
                        new FloatLiteral(2.5, null),
                        MultiplicationOperator.MULTIPLY,
                        new IntLiteral(2, null),
                        null
                )
        )
        expect:
        result in FloatValue
        TestUtils.isClose((result as FloatValue).getValue(), 5)
    }

    def "Should divide integers correctly"() {
        var result = evaluateExpression(
                new Multiplication(
                        new IntLiteral(11, null),
                        MultiplicationOperator.DIVIDE,
                        new IntLiteral(2, null),
                        null
                )
        )
        expect:
        result in IntValue
        (result as IntValue).getValue() == 5
    }

    def "Should divide floats correctly"() {
        var result = evaluateExpression(
                new Multiplication(
                        new FloatLiteral(5.5f, null),
                        MultiplicationOperator.DIVIDE,
                        new FloatLiteral(0.5f, null),
                        null
                )
        )
        expect:
        result in FloatValue
        TestUtils.isClose((result as FloatValue).getValue(), 11.0f)
    }

    def "Should modulo integers correctly"() {
        var result = evaluateExpression(
                new Multiplication(
                        new IntLiteral(5, null),
                        MultiplicationOperator.MODULO,
                        new IntLiteral(2, null),
                        null
                )
        )
        expect:
        result in IntValue
        (result as IntValue).getValue() == 1
    }

    def "Should modulo floats correctly"() {
        var result = evaluateExpression(
                new Multiplication(
                        new FloatLiteral(0.5, null),
                        MultiplicationOperator.MODULO,
                        new FloatLiteral(0.3, null),
                        null
                )
        )
        expect:
        result in FloatValue
        TestUtils.isClose((result as FloatValue).getValue(), 0.2f)
    }

    def "Should throw when dividing by zero"() {
        when:
        var result = evaluateExpression(
                new Multiplication(
                        new IntLiteral(1, null),
                        MultiplicationOperator.DIVIDE,
                        new FloatLiteral(0.0f, null),
                        null
                )
        )
        then:
        ExpressionEvaluationException e = thrown()
    }

    def "Should throw when modulo by zero"() {
        when:
        var result = evaluateExpression(
                new Multiplication(
                        new IntLiteral(1, null),
                        MultiplicationOperator.MODULO,
                        new FloatLiteral(0.0f, null),
                        null
                )
        )
        then:
        ExpressionEvaluationException e = thrown()
    }

    def "Should evaluate sum and mul correctly"() {
        var result = evaluateExpression("2 * 2 + 0.3f")
        expect:
        result in FloatValue
        TestUtils.isClose((result as FloatValue).getValue(), 4.3f)
    }

    def "Should evaluate complex"() {
        var result = evaluateExpression("((3 / (2 + 5 % 3)) * 1 + (9 / (1 - 2)))")
        expect:
        result in IntValue
        TestUtils.isClose((result as IntValue).getValue(), -9)
    }

    def "Should evaluate to true"() {
        var result = evaluateExpression("2 + 2 == 4")
        expect:
        result in BooleanValue
        (result as BooleanValue).isTruthy()
    }

    def "Should evaluate to false"() {
        var result = evaluateExpression("2 + 2 != 4")
        expect:
        result in BooleanValue
        !(result as BooleanValue).isTruthy()
    }

    def "Should evaluate to true"() {
        var result = evaluateExpression("5 > 4")
        expect:
        result in BooleanValue
        (result as BooleanValue).isTruthy()
    }

    def "Should evaluate to false"() {
        var result = evaluateExpression("5 < 4")
        expect:
        result in BooleanValue
        !(result as BooleanValue).isTruthy()
    }

    def "Should evaluate to true"() {
        var result = evaluateExpression("5 >= 5")
        expect:
        result in BooleanValue
        (result as BooleanValue).isTruthy()
    }

    def "Should evaluate to true"() {
        var result = evaluateExpression("5 <= 6")
        expect:
        result in BooleanValue
        (result as BooleanValue).isTruthy()
    }

    def "Should evaluate conjunction correctly"() {
        expect:
        result == (evaluateExpression(expression) as BooleanValue)
        where:
        expression        | result
        "true and true"   | new BooleanValue(true)
        "false and true"  | new BooleanValue(false)
        "true and false"  | new BooleanValue(false)
        "false and false" | new BooleanValue(false)
    }

    def "Should evaluate alternative correctly"() {
        expect:
        result == (evaluateExpression(expression) as BooleanValue)
        where:
        expression       | result
        "true or true"   | new BooleanValue(true)
        "false or true"  | new BooleanValue(true)
        "true or false"  | new BooleanValue(true)
        "false or false" | new BooleanValue(false)
    }

    def "Should evaluate negation correctly"() {
        expect:
        result == (evaluateExpression(expression) as BooleanValue)
        where:
        expression | result
        "true"     | new BooleanValue(true)
        "!true"    | new BooleanValue(false)
        "false"    | new BooleanValue(false)
        "!false"   | new BooleanValue(true)
    }

    def "Should evaluate identifiers correctly"() {
        expect:
        result == evaluateExpression(expression)
        where:
        expression | result
        "a"        | new IntValue(1)
        "a + a"    | new IntValue(2)
        "a > 0"    | new BooleanValue(true)
        "a == b"   | new BooleanValue(false)
        "a != 1"   | new BooleanValue(false)
        "b"        | new FloatValue(1.5f)
        "c"        | new StringValue("abc")
        "d"        | new BooleanValue(true)
    }
}
