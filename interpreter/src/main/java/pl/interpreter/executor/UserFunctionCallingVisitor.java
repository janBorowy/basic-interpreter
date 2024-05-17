package pl.interpreter.executor;

import lombok.Getter;
import pl.interpreter.executor.exceptions.InitializationException;
import pl.interpreter.executor.exceptions.ValueTypeException;
import pl.interpreter.parser.Assignment;
import pl.interpreter.parser.Block;
import pl.interpreter.parser.FunctionCall;
import pl.interpreter.parser.FunctionVisitor;
import pl.interpreter.parser.IfStatement;
import pl.interpreter.parser.Initialization;
import pl.interpreter.parser.Instruction;
import pl.interpreter.parser.MatchStatement;
import pl.interpreter.parser.ReturnStatement;
import pl.interpreter.parser.WhileStatement;

public class UserFunctionCallingVisitor implements FunctionVisitor {

    private final Environment environment;
    @Getter
    private Value returnedValue;
    private boolean functionReturned;

    public UserFunctionCallingVisitor(Environment environment) {
        this.environment = environment;
        functionReturned = false;
        returnedValue = null;
    }

    @Override
    public void visit(Block block) {
        block.getInstructions().stream()
                .takeWhile(it -> !functionReturned)
                .forEach(this::visit);
    }

    @Override
    public void visit(FunctionCall functionCall) {
        environment.runFunction(functionCall.getFunctionId(),
                ExpressionUtils.evaluateExpressionListInEnvironment(functionCall.getArguments(), environment));
    }

    @Override
    public void visit(Assignment assignment) {
        var value = ExpressionUtils.evaluateExpressionInEnvironment(assignment.getExpression(), environment);
        environment.getCurrentContext().setVariableForClosestScope(assignment.getId(), value);
    }

    @Override
    public void visit(Initialization initialization) {
        var value = ExpressionUtils.evaluateExpressionInEnvironment(initialization.getExpression(), environment);
        var variableType = ASTUtils.valueTypeFromVariableType(initialization.getType(), initialization.getUserType());
        if (!variableType.isTypeOf(value)) {
            throw new InitializationException("Value type %s does not match variable type %s".formatted(TypeUtils.getTypeOf(value), variableType));
        }
        environment.getCurrentContext().initializeVariableForClosestScope(initialization.getId(), new Variable(value, initialization.isVar()));
    }

    @Override
    public void visit(ReturnStatement statement) {
        returnedValue = ExpressionUtils.evaluateExpressionInEnvironment(statement.getExpression(), environment);
        functionReturned = true;
    }

    @Override
    public void visit(IfStatement statement) {
        var value = ExpressionUtils.evaluateExpressionInEnvironment(statement.getExpression(), environment);
        if (!(value instanceof BooleanValue condition)) {
            throw new ValueTypeException("Expected boolean, got " + TypeUtils.getTypeOf(value));
        }
        if (condition.isTruthy()) {
            visit(statement.getInstruction());
        } else if (statement.getElseInstruction() != null) {
            visit(statement.getElseInstruction());
        }
    }

    @Override
    public void visit(WhileStatement statement) {

    }

    @Override
    public void visit(MatchStatement statement) {

    }

    @Override
    public void visit(Instruction instruction) {
        switch (instruction) {
            case Assignment a -> visit(a);
            case Block b -> visit(b);
            case FunctionCall fc -> visit(fc);
            case IfStatement is -> visit(is);
            case Initialization i -> visit(i);
            case MatchStatement m -> visit(m);
            case ReturnStatement r -> visit(r);
            case WhileStatement w -> visit(w);
            default -> throw new IllegalStateException("Unknown instruction implementation: " + instruction);
        }
    }
}
