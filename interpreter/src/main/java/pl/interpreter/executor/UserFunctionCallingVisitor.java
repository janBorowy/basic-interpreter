package pl.interpreter.executor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import pl.interpreter.executor.exceptions.AssignmentException;
import pl.interpreter.executor.exceptions.FunctionCallException;
import pl.interpreter.executor.exceptions.InitializationException;
import pl.interpreter.executor.exceptions.InterpretationException;
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
import pl.interpreter.parser.Position;
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
        try {
            environment.runFunction(functionCall.getFunctionId(),
                    ExpressionUtils.evaluateExpressionListInEnvironment(functionCall.getArguments(), environment));
        } catch(FunctionCallException e) {
            throw new InterpretationException(e.getMessage(), functionCall.getPosition());
        }
    }

    @Override
    public void visit(Assignment assignment) {
        var value = ExpressionUtils.evaluateExpressionInEnvironment(assignment.getExpression(), environment);
        try {
            new AssignmentExecutor(assignment.getId(), value, environment).assign();
        } catch (AssignmentException e) {
            throw new InterpretationException(e.getMessage(), assignment.getPosition());
        }
    }

    @Override
    public void visit(Initialization initialization) {
        var value = ExpressionUtils.evaluateExpressionInEnvironment(initialization.getExpression(), environment);
        var variableType = ASTUtils.valueTypeFromVariableType(initialization.getType(), initialization.getUserType());
        try {
            new InitializationExecutor(initialization.getId(), variableType, value, initialization.isVar(), environment).execute();
        } catch (InitializationException e) {
            throw new InterpretationException(e.getMessage(), initialization.getPosition());
        }
    }

    @Override
    public void visit(ReturnStatement statement) {
        returnedValue = Optional.ofNullable(statement.getExpression())
                .map(it -> ExpressionUtils.evaluateExpressionInEnvironment(statement.getExpression(), environment))
                .orElse(null);
        functionReturned = true;
    }

    @Override
    public void visit(IfStatement statement) {
        try {
            var condition = ExpressionUtils.evaluteExpectingBooleanValue(statement.getExpression(), environment);
            if (condition.isTruthy()) {
                visit(statement.getInstruction());
            } else if (statement.getElseInstruction() != null) {
                visit(statement.getElseInstruction());
            }
        } catch (ValueTypeException e) {
            throw new InterpretationException(e.getMessage(), statement.getPosition());
        }
    }

    @Override
    public void visit(WhileStatement statement) {
        try {
            var condition = ExpressionUtils.evaluteExpectingBooleanValue(statement.getExpression(), environment);
            while (condition.isTruthy() && !functionReturned) {
                visit(statement.getInstruction());
                condition = ExpressionUtils.evaluteExpectingBooleanValue(statement.getExpression(), environment);
            }
        } catch (ValueTypeException e) {
            throw new InterpretationException(e.getMessage(), statement.getPosition());
        }
    }

    @Override
    public void visit(MatchStatement statement) {
        var value = ExpressionUtils.evaluateExpressionInEnvironment(statement.getExpression(), environment);
        var referencedValue = (ReferenceUtils.getReferencedValue(value));
        if (!(referencedValue instanceof VariantValue variantValue)) {
            throw new InterpretationException("Expected variant type, but got " + TypeUtils.getTypeOf(value), statement.getPosition());
        }
        statement.getBranches().stream()
                        .forEach(it -> checkIfStructureBelongsToVariant(it, variantValue.getVariantId()));
        visitMatchBranch(value, statement.getBranches(), statement.getPosition());
    }

    private void checkIfStructureBelongsToVariant(MatchBranch branch, String variantId) {
        if (Objects.isNull(branch.getStructureId())) {
            return;
        }
        var function = environment.getFunction(branch.getStructureId())
                .orElseThrow(() -> new InterpretationException("Structure %s does not exist".formatted(branch.getStructureId()), branch.getPosition()));
        if (!(function instanceof StructureConstructor sc)) {
            throw new InterpretationException("Structure %s does not exist".formatted(branch.getStructureId()), branch.getPosition());
        }
        var variant = environment.getVariant(variantId).get();
        if (!ValueMatcher.structureValueIsOfVariant(sc.getStructureName(), variant)) {
            throw new InterpretationException("Structure %s does not belong to variant %s".formatted(branch.getStructureId(), variantId), branch.getPosition());
        }
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

    private void visitMatchBranch(Value value, List<MatchBranch> branches, Position position) {
        branches.stream()
                .filter(it -> Objects.nonNull(it.getStructureId()))
                .filter(it -> it.getStructureId().equals(getReferencedStructureValue(value).getStructureId()))
                .findFirst()
                .ifPresentOrElse(it -> initializeNewVariableAndVisit(it.getInstruction(), it.getFieldName(),
                                new Variable(getReferencedStructureValue(value), false), position),
                        () -> visitDefaultBranch((VariantValue) ReferenceUtils.getReferencedValue(value), branches, position));
    }

    private StructureValue getReferencedStructureValue(Value value) {
        return (((VariantValue) ReferenceUtils.getReferencedValue(value)).getStructureValue());
    }

    private void initializeNewVariableAndVisit(Instruction instruction, String variableId, Variable variableToSet, Position position) {
        try {
            environment.getCurrentContext().initializeVariableForClosestScope(variableId, variableToSet);
            visit(instruction);
        } catch (InitializationException e) {
            throw new InterpretationException(e.getMessage(), position);
        }
    }

    private void visitDefaultBranch(VariantValue value, List<MatchBranch> branches, Position position) {
        branches.stream()
                .filter(it -> Objects.isNull(it.getStructureId()))
                .findFirst()
                .ifPresentOrElse(it -> visit(it.getInstruction()),
                        () -> {
                            throw new InterpretationException("Value did not match any branch " + value, position);
                        });
    }
}
