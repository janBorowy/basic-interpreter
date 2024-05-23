package pl.interpreter.executor;

import java.util.List;
import lombok.experimental.UtilityClass;
import pl.interpreter.executor.exceptions.ExpressionEvaluationException;
import pl.interpreter.executor.exceptions.InterpretationException;
import pl.interpreter.executor.exceptions.ValueTypeException;
import pl.interpreter.parser.Expression;
import pl.interpreter.parser.Identifier;

@UtilityClass
public class ExpressionUtils {

    public BooleanValue evaluteExpectingBooleanValue(Expression expression, Environment environment) {
        var value = ExpressionUtils.evaluateExpressionInEnvironment(expression, environment);
        if (!(value instanceof BooleanValue booleanValue)) {
            throw new ValueTypeException("Expected boolean, got " + TypeUtils.getTypeOf(value));
        }
        return booleanValue;
    }

    public List<Value> evaluateExpressionListInEnvironment(List<Expression> expressions, Environment environment) {
        return expressions.stream()
                .map(it -> ExpressionUtils.evaluateExpressionInEnvironment(it, environment))
                .toList();
    }

    public Value evaluateExpressionInEnvironment(Expression expression, Environment environment) {
        if (returnsReference(expression)) {
            var variable = environment.getCurrentContext().resolveVariable(((Identifier)expression).getValue());
            return new Reference(ReferenceUtils.getReferencedValue(variable.getValue()), variable.isMutable());
        }
        var visitor = new ExpressionEvaluatingVisitor(environment);
        visitor.visit(expression);
        return visitor.getValue();
    }

    private boolean returnsReference(Expression expression) {
        return expression instanceof Identifier;
    }
}
