package pl.interpreter.parser;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;

public class PrintVisitor implements StatementVisitor {

    private record Param(String name, String value) {}

    public final Writer writer;

    private static final String BRANCH_SYMBOL = "|";
    private static final String NAME_PREFIX = "-";
    private static final String DEPTH_SYMBOL = " ";
    private static final int NON_DEPTH_PREFIX_LENGTH = NAME_PREFIX.length() + BRANCH_SYMBOL.length();
    private int depth = 0;

    private static final String ID_MSG = "id";
    private static final String TYPE_MSG = "type";
    private static final String VALUE_MSG = "value";
    private static final String OPERATOR_MSG = "operator";
    private static final String FUNCTION_ID_MSG = "function_id";
    private static final String FIELD_NAME_MSG = "field_name";
    private static final String RETURN_TYPE_MSG = "return_type";
    private static final String USER_TYPE_MSG = "user_type";
    private static final String VAR_MSG = "var";

    public PrintVisitor(@NonNull Writer writer) {
        this.writer = writer;
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        printNode(returnStatement, List.of());
        if (returnStatement.getExpression() != null) {
            diveIn();
            visit(returnStatement.getExpression());
            diveOut();
        }
    }

    @Override
    public void visit(Value expression) {
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
    public void visit(Sum sum) {
        printNode(sum, List.of(
                new Param(OPERATOR_MSG, '"' + sum.getOperator().toString() + '"')));
        diveIn();
        visit(sum.getLeft());
        visit(sum.getRight());
        diveOut();
    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
        printNode(booleanLiteral, List.of(
                new Param(VALUE_MSG, String.valueOf(booleanLiteral.isTruthy()))));
    }

    @Override
    public void visit(FloatLiteral floatLiteral) {
        printNode(floatLiteral, List.of(
                new Param(VALUE_MSG, String.valueOf(floatLiteral.getValue()))));
    }

    @Override
    public void visit(IntLiteral intLiteral) {
        printNode(intLiteral, List.of(
                new Param(VALUE_MSG, String.valueOf(intLiteral.getValue()))));
    }

    @Override
    public void visit(Multiplication multiplication) {
        printNode(multiplication, List.of(
                new Param(OPERATOR_MSG, '"' + multiplication.getOperator().toString() + '"')));
        diveIn();
        visit(multiplication.getLeft());
        visit(multiplication.getRight());
        diveOut();
    }

    @Override
    public void visit(Negation negation) {
        printNode(negation, List.of());
        diveIn();
        visit(negation.getExpression());
        diveOut();
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        printNode(stringLiteral, List.of(
                new Param(VALUE_MSG, stringLiteral.getValue())));
    }

    @Override
    public void visit(DotAccess dotAccess) {
        printNode(dotAccess, List.of(
                new Param(FIELD_NAME_MSG, dotAccess.getFieldName())));
        diveIn();
        visit(dotAccess.getExpression());
        diveOut();
    }

    @Override
    public void visit(Identifier identifier) {
        printNode(identifier, List.of(
                new Param(ID_MSG, identifier.getValue())));
    }

    @Override
    public void visit(FunctionCall functionCall) {
        printNode(functionCall, List.of(
                new Param(FUNCTION_ID_MSG, functionCall.getFunctionId())));
        diveIn();
        functionCall.getArguments().forEach(this::visit);
        diveOut();
    }

    @Override
    public void visit(StructureDefinition structureDefinition) {
        printNode(structureDefinition, List.of(
                new Param(ID_MSG, structureDefinition.getId())));
        diveIn();
        structureDefinition.getParameters().forEach(this::visit);
        diveOut();
    }

    @Override
    public void visit(Program program) {
        printNode(program, List.of());
        diveIn();
        program.getDefinitions().values().stream().sorted(Comparator.comparing(Definition::getId)).forEach(this::visit);
        diveOut();
    }

    @Override
    public void visit(Definition definition) {
        switch(definition) {
            case StructureDefinition structureDefinition -> visit(structureDefinition);
            case VariantDefinition variantDefinition -> visit(variantDefinition);
            case FunctionDefinition functionDefinition -> visit(functionDefinition);
            default -> throw new UnknownNodeException();
        }
    }

    @Override
    public void visit(ParameterSignatureMap parameterSignatureMap) {
        parameterSignatureMap.forEach(this::printParameter);
    }

    @Override
    public void visit(VariantDefinition variantDefinition) {
        printNode(variantDefinition, List.of(
                new Param(ID_MSG, variantDefinition.getId())));
        diveIn();
        variantDefinition.getStructureIds().forEach(this::printUserType);
        diveOut();
    }

    @Override
    public void visit(FunctionDefinition functionDefinition) {
        var params = new ArrayList<>(List.of(new Param(ID_MSG, functionDefinition.getId()),
                new Param(RETURN_TYPE_MSG, functionDefinition.getReturnType().type().toString())));
        if (Objects.nonNull(functionDefinition.getReturnType().userType())) {
            params.add(new Param(USER_TYPE_MSG, functionDefinition.getReturnType().userType()));
        }
        printNode(functionDefinition, params);
        diveIn();
        functionDefinition.getParameters().forEach(this::printParameter);
        visit(functionDefinition.getBlock());
        diveOut();
    }

    @Override
    public void visit(Block block) {
        printNode(block, List.of());
        diveIn();
        block.getInstructions().forEach(this::visit);
        diveOut();
    }

    @Override
    public void visit(Instruction instruction) {
        switch(instruction) {
            case ReturnStatement returnStatement -> visit(returnStatement);
            case Assignment assignment -> visit(assignment);
            case FunctionCall functionCall -> visit(functionCall);
            case Initialization initialization -> visit(initialization);
            case IfStatement ifStatement -> visit(ifStatement);
            case Block block -> visit(block);
            case WhileStatement whileStatement -> visit(whileStatement);
            case MatchStatement matchStatement -> visit(matchStatement);
            default -> throw new UnknownNodeException();
        }
    }

    @Override
    public void visit(Assignment assignment) {
        printNode(assignment, List.of(
                new Param(ID_MSG, assignment.getId())));
        diveIn();
        visit(assignment.getExpression());
        diveOut();
    }

    @Override
    public void visit(Initialization initialization) {
        var params = new ArrayList<>(List.of(
                new Param(ID_MSG, initialization.getId()),
                new Param(VAR_MSG, String.valueOf(initialization.isVar())),
                new Param(TYPE_MSG, initialization.getType().toString()))
        );
        if (Objects.nonNull(initialization.getUserType())) {
            params.add(new Param(USER_TYPE_MSG, initialization.getUserType()));
        }
        printNode(initialization, params);
        diveIn();
        visit(initialization.getExpression());
        diveOut();
    }

    @Override
    public void visit(IfStatement ifStatement) {
        printNode(ifStatement, List.of());
        diveIn();
        visit(ifStatement.getExpression());
        visit(ifStatement.getInstruction());
        if (ifStatement.getElseInstruction() != null) {
            visit(ifStatement.getElseInstruction());
        }
        diveOut();
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        printNode(whileStatement, List.of());
        diveIn();
        visit(whileStatement.getExpression());
        visit(whileStatement.getInstruction());
        diveOut();
    }

    @Override
    public void visit(MatchStatement matchStatement) {
        printNode(matchStatement, List.of());
        diveIn();
        visit(matchStatement.getExpression());
        matchStatement.getBranches().forEach(this::visit);
        diveOut();
    }

    @Override
    public void visit(MatchBranch matchBranch) {
        printNode(matchBranch, List.of(
                new Param(TYPE_MSG, matchBranch.getStructureId()),
                new Param(FIELD_NAME_MSG, matchBranch.getFieldName())
        ));
        diveIn();
        visit(matchBranch.getInstruction());
        diveOut();
    }

    @Override
    public void visit(Parameter parameter) {
        printParameter(parameter.getId(), parameter.getType());
    }

    @Override
    public void visit(Alternative alternative) {
        printNode(alternative, List.of());
        diveIn();
        visit(alternative.getLeft());
        visit(alternative.getRight());
        diveOut();
    }

    @Override
    public void visit(Cast cast) {
        printNode(cast, List.of(
                new Param(TYPE_MSG, cast.getToType().toString())));
        diveIn();
        visit(cast.getExpression());
        diveOut();
    }

    @Override
    public void visit(Relation relation) {
        printNode(relation, List.of(
            new Param(OPERATOR_MSG, '"' + relation.getOperator().toString() + '"')));
        diveIn();
        visit(relation.getLeft());
        visit(relation.getRight());
        diveOut();
    }

    @Override
    public void visit(Conjunction conjunction) {
        printNode(conjunction, List.of());
        diveIn();
        visit(conjunction.getLeft());
        visit(conjunction.getRight());
        diveOut();
    }

    private String getPrefix() {
        if (depth - NON_DEPTH_PREFIX_LENGTH < 0) {
            return "";
        }
        return DEPTH_SYMBOL.repeat(depth - NON_DEPTH_PREFIX_LENGTH) +
                BRANCH_SYMBOL +
                NAME_PREFIX;
    }

    private void printNode(Statement statement, List<Param> params) {
        write(getPrefix() +
                statement.getClass().getSimpleName() +
                ' ' +
                "<row: " +
                statement.getPosition().row() +
                ", col: " +
                statement.getPosition().col() +
                "> " +
                getParametersString(params) +
                '\n');
    }

    private void printParameter(String id, ParameterType parameter) {
        var userTypeStr = "";
        if (Objects.nonNull(parameter.userType())) {
            userTypeStr = ", userType=" + parameter.userType();
        }
        write(getPrefix() +
                "Parameter" +
                ' ' +
                "id=" + id +
                ", type=" + parameter.variableType().toString() +
                userTypeStr +
                '\n');
    }

    private void printUserType(String id) {
        write(getPrefix() +
                "Type" +
                ' ' +
                "id=" + id +
                '\n');
    }

    private String getParametersString(List<Param> params) {
        return params.stream()
                .map(p -> getParamString(p.name(), p.value()))
                .collect(Collectors.joining(", "));
    }

    private String getParamString(String name, String value) {
        return name + "=" + value;
    }

    private void diveIn() {
        depth += NON_DEPTH_PREFIX_LENGTH;
    }

    private void diveOut() {
        depth -= NON_DEPTH_PREFIX_LENGTH;
    }

    private void write(String string) {
        try {
            writer.write(string);
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred when writing");
        }
    }
}
