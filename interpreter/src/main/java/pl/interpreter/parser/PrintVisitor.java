package pl.interpreter.parser;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
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

    public PrintVisitor(@NonNull Writer writer) {
        this.writer = writer;
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        printNode(returnStatement, List.of(), returnStatement.getPosition());
        if (returnStatement.getExpression() != null) {
            diveIn();
            visit(returnStatement.getExpression());
            diveOut();
        }
    }

    @Override
    public void visit(Expression expression) {
        if (expression instanceof Alternative alternative) {
            this.visit(alternative);
        } else if (expression instanceof Cast cast) {
            this.visit(cast);
        } else if (expression instanceof Conjunction conjunction) {
            this.visit(conjunction);
        } else if (expression instanceof Relation relation) {
            this.visit(relation);
        } else if (expression instanceof BooleanLiteral booleanLiteral) {
            this.visit(booleanLiteral);
        } else if (expression instanceof FloatLiteral floatLiteral) {
            this.visit(floatLiteral);
        } else if (expression instanceof IntLiteral intLiteral) {
            this.visit(intLiteral);
        } else if (expression instanceof Multiplication multiplication) {
            this.visit(multiplication);
        } else if (expression instanceof Negation negation) {
            this.visit(negation);
        } else if (expression instanceof ReturnStatement returnStatement) {
            this.visit(returnStatement);
        } else if (expression instanceof StringLiteral stringLiteral) {
            this.visit(stringLiteral);
        } else if (expression instanceof Sum sum) {
            this.visit(sum);
        } else if (expression instanceof FunctionCall functionCall) {
            this.visit(functionCall);
        } else if (expression instanceof DotAccess dotAccess) {
            this.visit(dotAccess);
        } else if (expression instanceof Identifier identifier) {
            this.visit(identifier);
        }
    }

    @Override
    public void visit(Sum sum) {
        printNode(sum, List.of(
                new Param(OPERATOR_MSG, '"' + sum.getOperator().toString() + '"')
        ), sum.getPosition());
        diveIn();
        this.visit(sum.getLeft());
        this.visit(sum.getRight());
        diveOut();
    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
        printNode(booleanLiteral, List.of(
                new Param(VALUE_MSG, String.valueOf(booleanLiteral.isOn()))
        ), booleanLiteral.getPosition());
    }

    @Override
    public void visit(FloatLiteral floatLiteral) {
        printNode(floatLiteral, List.of(
                new Param(VALUE_MSG, String.valueOf(floatLiteral.getValue()))
        ), floatLiteral.getPosition());
    }

    @Override
    public void visit(IntLiteral intLiteral) {
        printNode(intLiteral, List.of(
                new Param(VALUE_MSG, String.valueOf(intLiteral.getValue()))
        ), intLiteral.getPosition());
    }

    @Override
    public void visit(Multiplication multiplication) {
        printNode(multiplication, List.of(
                new Param(OPERATOR_MSG, '"' + multiplication.getOperator().toString() + '"')
        ), multiplication.getPosition());
        diveIn();
        this.visit(multiplication.getLeft());
        this.visit(multiplication.getRight());
        diveOut();
    }

    @Override
    public void visit(Negation negation) {
        printNode(negation, List.of(), negation.getPosition());
        diveIn();
        this.visit(negation.getExpression());
        diveOut();
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        printNode(stringLiteral, List.of(
                new Param(VALUE_MSG, stringLiteral.getValue())
        ), stringLiteral.getPosition());
    }

    @Override
    public void visit(DotAccess dotAccess) {
        printNode(dotAccess, List.of(
                new Param(FIELD_NAME_MSG, dotAccess.getFieldName())
        ), dotAccess.getPosition());
        diveIn();
        this.visit(dotAccess.getExpression());
        diveOut();
    }

    @Override
    public void visit(Identifier identifier) {
        printNode(identifier, List.of(
                new Param(ID_MSG, identifier.getValue())
        ), identifier.getPosition());
    }

    @Override
    public void visit(FunctionCall functionCall) {
        printNode(functionCall, List.of(
                new Param(FUNCTION_ID_MSG, functionCall.getFunctionId())
        ), functionCall.getPosition());
        diveIn();
        functionCall.getParameters().forEach(this::visit);
        diveOut();
    }

    @Override
    public void visit(Alternative alternative) {
        printNode(alternative, List.of(), alternative.getPosition());
        diveIn();
        this.visit(alternative.getLeft());
        this.visit(alternative.getRight());
        diveOut();
    }

    @Override
    public void visit(Cast cast) {
        printNode(cast, List.of(
                new Param(TYPE_MSG, cast.getToType().toString())
        ), cast.getPosition());
        diveIn();
        this.visit(cast.getExpression());
        diveOut();
    }

    @Override
    public void visit(Relation relation) {
        printNode(relation, List.of(
            new Param(OPERATOR_MSG, '"' + relation.getOperator().toString() + '"')
        ), relation.getPosition());
        diveIn();
        this.visit(relation.getLeft());
        this.visit(relation.getRight());
        diveOut();
    }

    @Override
    public void visit(Conjunction conjunction) {
        printNode(conjunction, List.of(), conjunction.getPosition());
        diveIn();
        this.visit(conjunction.getLeft());
        this.visit(conjunction.getRight());
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
                getParametersString(params) +
                '\n');
    }

    private void printNode(Statement statement, List<Param> params, Position position) {
        write(getPrefix() +
                statement.getClass().getSimpleName() +
                ' ' +
                "<row: " +
                position.row() +
                ", col: " +
                position.col() +
                "> " +
                getParametersString(params) +
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
