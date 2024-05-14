package pl.interpreter.executor;

import lombok.Getter;
import pl.interpreter.executor.exceptions.InvalidValueTypeException;
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
import pl.interpreter.parser.Relation;
import pl.interpreter.parser.StringLiteral;
import pl.interpreter.parser.Sum;
import pl.interpreter.parser.UnknownNodeException;

@Getter
public class ExpressionEvaluatingVisitor implements ExpressionVisitor {

    private Value value;

    @Override
    public void visit(Expression expression) {
        try {
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
        } catch (InvalidValueTypeException exception) {

        }
    }

    @Override
    public void visit(Alternative alternative) {

    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
        value = new BooleanValue(booleanLiteral.isTruthy());
    }

    @Override
    public void visit(Cast cast) {

    }

    @Override
    public void visit(Conjunction conjunction) {

    }

    @Override
    public void visit(DotAccess dotAccess) {

    }

    @Override
    public void visit(FloatLiteral floatLiteral) {
        value = new FloatValue(floatLiteral.getValue());
    }

    @Override
    public void visit(FunctionCall functionCall) {

    }

    @Override
    public void visit(Identifier identifier) {

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
        var operator = switch(multiplication.getOperator()){
            case MULTIPLY -> MultiplicationEvaluator.MultiplicationOperator.MULTIPLICATION;
            case DIVIDE -> MultiplicationEvaluator.MultiplicationOperator.DIVISION;
            case MODULO -> MultiplicationEvaluator.MultiplicationOperator.MODULO;
        };
        value = new MultiplicationEvaluator(leftHandSide, value, operator).evaluate();
    }

    @Override
    public void visit(Negation negation) {

    }

    @Override
    public void visit(Relation relation) {

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
        var operator = switch(sum.getOperator()) {
            case PLUS -> SumEvaluator.SumOperator.PLUS;
            case MINUS -> SumEvaluator.SumOperator.MINUS;
        };
        value = new SumEvaluator(leftHandSide, value, operator).evaluate();
    }
}
