package pl.interpreter.executor;

import lombok.Getter;
import pl.interpreter.executor.exceptions.AccessException;
import pl.interpreter.executor.exceptions.EnvironmentException;
import pl.interpreter.executor.exceptions.ExpressionEvaluationException;
import pl.interpreter.executor.exceptions.FunctionCallException;
import pl.interpreter.executor.exceptions.InterpretationException;
import pl.interpreter.executor.exceptions.ParameterTypeException;
import pl.interpreter.executor.exceptions.ValueTypeException;
import pl.interpreter.parser.Alternative;
import pl.interpreter.parser.BooleanLiteral;
import pl.interpreter.parser.Cast;
import pl.interpreter.parser.Conjunction;
import pl.interpreter.parser.DotAccess;
import pl.interpreter.parser.Expression;
import pl.interpreter.parser.ExpressionVisitor;
import pl.interpreter.parser.FloatLiteral;
import pl.interpreter.parser.FunctionCall;
import pl.interpreter.parser.Identifier;
import pl.interpreter.parser.IntLiteral;
import pl.interpreter.parser.Multiplication;
import pl.interpreter.parser.Negation;
import pl.interpreter.parser.Position;
import pl.interpreter.parser.Relation;
import pl.interpreter.parser.StringLiteral;
import pl.interpreter.parser.Sum;
import pl.interpreter.parser.UnknownNodeException;

@Getter
public class ExpressionEvaluatingVisitor implements ExpressionVisitor {

    private Value value;
    private final Environment environment;

    public ExpressionEvaluatingVisitor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void visit(Expression expression) {
        switch (expression) {
            case Alternative alternative -> visit(alternative);
            case Cast cast -> visit(cast);
            case Conjunction conjunction -> visit(conjunction);
            case Relation relation -> visit(relation);
            case BooleanLiteral booleanLiteral -> visit(booleanLiteral);
            case FloatLiteral floatLiteral -> visit(floatLiteral);
            case IntLiteral intLiteral -> visit(intLiteral);
            case Multiplication multiplication -> visit(multiplication);
            case Negation negation -> visit(negation);
            case StringLiteral stringLiteral -> visit(stringLiteral);
            case Sum sum -> visit(sum);
            case FunctionCall functionCall -> visit(functionCall);
            case DotAccess dotAccess -> visit(dotAccess);
            case Identifier identifier -> visit(identifier);
            default -> throw new UnknownNodeException();
        }
    }

    @Override
    public void visit(Alternative alternative) {
        visit(alternative.getLeft());
        var lhs = value;
        if (ConjunctionOrAlternativeEvaluator.shouldShortCircuit(value, ConjunctionOrAlternativeEvaluator.Operator.ALTERNATIVE)) {
            value = new BooleanValue(true);
            return;
        }
        visit(alternative.getRight());
        try {
            value = new ConjunctionOrAlternativeEvaluator(lhs, value, ConjunctionOrAlternativeEvaluator.Operator.ALTERNATIVE).evaluate();
        } catch (ValueTypeException e) {
            throw new InterpretationException(e.getMessage(), alternative.getPosition());
        }
    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
        value = new BooleanValue(booleanLiteral.isTruthy());
    }

    @Override
    public void visit(Cast cast) {
        visit(cast.getExpression());
        var castType = switch (cast.getToType()) {
            case INT -> CastEvaluator.LegalCastType.INT;
            case FLOAT -> CastEvaluator.LegalCastType.FLOAT;
            case STRING -> CastEvaluator.LegalCastType.STRING;
            case BOOL -> CastEvaluator.LegalCastType.BOOLEAN;
        };
        try {
            value = new CastEvaluator(value, castType).evaluate();
        } catch (ValueTypeException e) {
            throw new InterpretationException(e.getMessage(), cast.getPosition());
        } catch (NumberFormatException e) {
            throw new InterpretationException("Number format error " + e.getMessage().toLowerCase(), cast.getPosition());
        }
    }

    @Override
    public void visit(Conjunction conjunction) {
        visit(conjunction.getLeft());
        var lhs = value;
        if (ConjunctionOrAlternativeEvaluator.shouldShortCircuit(value, ConjunctionOrAlternativeEvaluator.Operator.CONJUNCTION)) {
            value = new BooleanValue(false);
            return;
        }
        visit(conjunction.getRight());
        try {
            value = new ConjunctionOrAlternativeEvaluator(lhs, value, ConjunctionOrAlternativeEvaluator.Operator.CONJUNCTION).evaluate();
        } catch (ValueTypeException e) {
            throw new InterpretationException(e.getMessage(), conjunction.getPosition());
        }
    }

    @Override
    public void visit(DotAccess dotAccess) {
        visit(dotAccess.getExpression());
        try {
            value = new DotAccessEvaluator(value, dotAccess.getFieldName()).evaluate();
        } catch (ValueTypeException | AccessException e) {
            throw new InterpretationException(e.getMessage(), dotAccess.getPosition());
        }
    }

    @Override
    public void visit(FloatLiteral floatLiteral) {
        value = new FloatValue(floatLiteral.getValue());
    }

    @Override
    public void visit(FunctionCall functionCall) {
        var arguments = ExpressionUtils.evaluateExpressionListInEnvironment(functionCall.getArguments(), environment);
        try {
            value = environment.runFunction(functionCall.getFunctionId(), arguments);
            if (value == null) {
                throw new FunctionCallException("Function returns void, but value is expected");
            }
        } catch (FunctionCallException | ParameterTypeException e) {
            throw new InterpretationException(e.getMessage(), functionCall.getPosition());
        }
    }

    @Override
    public void visit(Identifier identifier) {
        try {
            value = environment.getCurrentContext().resolveVariable(identifier.getValue()).getValue();
        } catch (EnvironmentException e) {
            throw new InterpretationException(e.getMessage(), identifier.getPosition());
        }
    }

    @Override
    public void visit(IntLiteral intLiteral) {
        value = new IntValue(intLiteral.getValue());
    }

    @Override
    public void visit(Multiplication multiplication) {
        visit(multiplication.getLeft());
        var leftHandSide = value;
        visit(multiplication.getRight());
        var operator = switch (multiplication.getOperator()) {
            case MULTIPLY -> MultiplicationEvaluator.Operator.MULTIPLICATION;
            case DIVIDE -> MultiplicationEvaluator.Operator.DIVISION;
            case MODULO -> MultiplicationEvaluator.Operator.MODULO;
        };
        try {
            value = new MultiplicationEvaluator(leftHandSide, value, operator).evaluate();
        } catch (ValueTypeException | ExpressionEvaluationException e) {
            throw new InterpretationException(e.getMessage(), multiplication.getPosition());
        }
    }

    @Override
    public void visit(Negation negation) {
        visit(negation.getExpression());
        try {
            value = new NegationEvaluator(value).evaluate();
        } catch (ValueTypeException e) {
            throw new InterpretationException(e.getMessage(), negation.getPosition());
        }
    }

    @Override
    public void visit(Relation relation) {
        visit(relation.getLeft());
        var leftHandSide = value;
        visit(relation.getRight());
        var operator = switch (relation.getOperator()) {
            case EQUALS -> RelationEvaluator.Operator.EQUAL;
            case NOT_EQUALS -> RelationEvaluator.Operator.NOT_EQUAL;
            case LESS_THAN -> RelationEvaluator.Operator.LESS_THAN;
            case GREATER_THAN -> RelationEvaluator.Operator.GREATER_THAN;
            case LESS_THAN_OR_EQUALS -> RelationEvaluator.Operator.LESS_THAN_OR_EQUAL;
            case GREATER_THAN_OR_EQUALS -> RelationEvaluator.Operator.GREATER_THAN_OR_EQUAL;
        };
        try {
            value = new RelationEvaluator(leftHandSide, value, operator).evaluate();
        } catch (ExpressionEvaluationException e) {
            throw new InterpretationException(e.getMessage(), relation.getPosition());
        }
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        value = new StringValue(stringLiteral.getValue());
    }

    @Override
    public void visit(Sum sum) {
        visit(sum.getLeft());
        var leftHandSide = value;
        visit(sum.getRight());
        var operator = switch (sum.getOperator()) {
            case PLUS -> SumEvaluator.Operator.PLUS;
            case MINUS -> SumEvaluator.Operator.MINUS;
        };
        try {
            value = new SumEvaluator(leftHandSide, value, operator).evaluate();
        } catch (ExpressionEvaluationException | ValueTypeException e) {
            throw new InterpretationException(e.getMessage(), sum.getPosition());
        }
    }
}
