package pl.interpreter.executor;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import pl.interpreter.executor.exceptions.AssignmentException;
import pl.interpreter.executor.exceptions.InitializationException;
import pl.interpreter.executor.exceptions.MatchStatementException;
import pl.interpreter.executor.exceptions.ValueTypeException;
import pl.interpreter.parser.Assignment;
import pl.interpreter.parser.Block;
import pl.interpreter.parser.FunctionCall;
import pl.interpreter.parser.FunctionVisitor;
import pl.interpreter.parser.IfStatement;
import pl.interpreter.parser.Initialization;
import pl.interpreter.parser.Instruction;
import pl.interpreter.parser.MatchBranch;
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
        if (value instanceof StructureValue structure) {
            var type = TypeUtils.getTypeOf(environment.getCurrentContext().resolveVariable(assignment.getId()).getValue());
            if (TypeUtils.isVariant(type, environment)) {
                if (TypeUtils.structureIsVariant(structure, type.getUserType(), environment)) {
                    environment.getCurrentContext().setVariableForClosestScope(assignment.getId(), new VariantValue(type.getUserType(), structure));
                    return;
                } else {
                    throw new AssignmentException("Value type %s does not match variable type %s".formatted(TypeUtils.getTypeOf(value), type));
                }
            } else {
                if (type.getType() == ValueType.Type.USER_TYPE && Objects.equals(structure.getStructureId(), type.getUserType())) {
                    throw new AssignmentException("Value type %s does not match variable type %s".formatted(TypeUtils.getTypeOf(value), type));
                }
            }
        }
        if (value instanceof VariantValue variant) {
            var type = TypeUtils.getTypeOf(environment.getCurrentContext().resolveVariable(assignment.getId()).getValue());
            if (!Objects.equals(type.getUserType(), variant.getVariantId())) {
                throw new AssignmentException("Value type %s does not match variable type %s".formatted(TypeUtils.getTypeOf(value), type));
            }
        }
        environment.getCurrentContext().setVariableForClosestScope(assignment.getId(), value);
    }

    @Override
    public void visit(Initialization initialization) {
        var value = ExpressionUtils.evaluateExpressionInEnvironment(initialization.getExpression(), environment);
        var variableType = ASTUtils.valueTypeFromVariableType(initialization.getType(), initialization.getUserType());
        if (!variableType.isTypeOf(value)) {
            throw new InitializationException("Value type %s does not match variable type %s".formatted(TypeUtils.getTypeOf(value), variableType));
        }
        // TODO: refactor this
        if (TypeUtils.isVariant(variableType, environment)) {
            initializeForVariant(value, variableType, initialization.getId(), initialization.isVar());
            return;
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
        var condition = ExpressionUtils.evaluteExpectingBooleanValue(statement.getExpression(), environment);
        if (condition.isTruthy()) {
            openNewScopeAndVisit(statement.getInstruction());
        } else if (statement.getElseInstruction() != null) {
            openNewScopeAndVisit(statement.getElseInstruction());
        }
    }

    @Override
    public void visit(WhileStatement statement) {
        var condition = ExpressionUtils.evaluteExpectingBooleanValue(statement.getExpression(), environment);
        while (condition.isTruthy()) {
            openNewScopeAndVisit(statement.getInstruction());
            condition = ExpressionUtils.evaluteExpectingBooleanValue(statement.getExpression(), environment);
        }
    }

    @Override
    public void visit(MatchStatement statement) {
        var value = ExpressionUtils.evaluateExpressionInEnvironment(statement.getExpression(), environment);
        if (!(value instanceof VariantValue variant)) {
            throw new ValueTypeException("Expected variant type, but got " + TypeUtils.getTypeOf(value));
        }

        visitMatchBranch(variant, statement.getBranches());
    }

    @Override
    public void visit(Instruction instruction) {
        switch (instruction) {
            case Assignment a -> visit(a);
            case Block b -> {
                environment.getCurrentContext().openNewScope();
                visit(b);
                environment.getCurrentContext().closeClosestScope();
            }
            case FunctionCall fc -> visit(fc);
            case IfStatement is -> visit(is);
            case Initialization i -> visit(i);
            case MatchStatement m -> visit(m);
            case ReturnStatement r -> visit(r);
            case WhileStatement w -> visit(w);
            default -> throw new IllegalStateException("Unknown instruction implementation: " + instruction);
        }
    }

    private void openNewScopeAndVisit(Instruction instruction) {
        environment.getCurrentContext().openNewScope();
        visit(instruction);
        environment.getCurrentContext().closeClosestScope();
    }

    private void openNewScopeVisitAndInitializeVariable(Instruction instruction, String variableId, Variable variableToSet) {
        environment.getCurrentContext().openNewScope();
        environment.getCurrentContext().initializeVariableForClosestScope(variableId, variableToSet);
        visit(instruction);
        environment.getCurrentContext().closeClosestScope();
    }

    private void visitMatchBranch(VariantValue value, List<MatchBranch> branches) {
        branches.stream()
                .filter(it -> it.getStructureId().equals(value.getStructureValue().getStructureId()))
                .findFirst()
                .ifPresentOrElse(it -> openNewScopeVisitAndInitializeVariable(it.getInstruction(), it.getFieldName(),
                                new Variable(value.getStructureValue(), false)),
                        () -> visitDefaultBranch(value, branches));
    }

    private void visitDefaultBranch(VariantValue value, List<MatchBranch> branches) {
        branches.stream()
                .filter(it -> Objects.isNull(it.getStructureId()))
                .findFirst()
                .ifPresentOrElse(it -> openNewScopeAndVisit(it.getInstruction()),
                        () -> {
                            throw new MatchStatementException("Value did not match any branch " + value);
                        });
    }

    private void initializeForVariant(Value value, ValueType variantType, String id, boolean isVar) {
        if (value instanceof StructureValue structure) {
            if (TypeUtils.structureIsVariant(structure, variantType.getUserType(), environment)) {
                environment.getCurrentContext().initializeVariableForClosestScope(id,
                        new Variable(new VariantValue(variantType.getUserType(), structure), isVar));
            } else {
                throw new InitializationException(
                        "Value type %s does not belong to variant %s".formatted(TypeUtils.getTypeOf(value), variantType));
            }
        } else if (value instanceof VariantValue variant && !(variant.getVariantId().equals(variantType.getUserType()))) {
            throw new InitializationException(
                    "Value type %s does not match variable type %s".formatted(TypeUtils.getTypeOf(value), variantType));
        }
    }
}
