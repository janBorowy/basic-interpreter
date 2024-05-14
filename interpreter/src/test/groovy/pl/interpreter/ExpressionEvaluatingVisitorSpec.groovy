package pl.interpreter

import pl.interpreter.executor.BooleanValue
import pl.interpreter.executor.ExpressionEvaluatingVisitor
import pl.interpreter.executor.FloatValue
import pl.interpreter.executor.IntValue
import pl.interpreter.executor.StringValue
import pl.interpreter.executor.exceptions.ExpressionEvaluationException
import pl.interpreter.executor.exceptions.InvalidValueTypeException
import pl.interpreter.parser.AdditionOperator
import pl.interpreter.parser.BooleanLiteral
import pl.interpreter.parser.Expression
import pl.interpreter.parser.FloatLiteral
import pl.interpreter.parser.IntLiteral
import pl.interpreter.parser.Multiplication
import pl.interpreter.parser.MultiplicationOperator
import pl.interpreter.parser.StringLiteral
import pl.interpreter.parser.Sum
import spock.lang.Specification

class ExpressionEvaluatingVisitorSpec extends Specification {

    def evaluateExpression(Expression expression) {
        var visitor = new ExpressionEvaluatingVisitor()
        visitor.visit(expression)
        return visitor.getValue()
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
}
