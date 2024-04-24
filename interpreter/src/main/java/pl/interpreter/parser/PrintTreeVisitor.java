package pl.interpreter.parser;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import pl.interpreter.parser.ast.AdditiveOperator;
import pl.interpreter.parser.ast.AfterIdentifierStatement;
import pl.interpreter.parser.ast.As;
import pl.interpreter.parser.ast.Block;
import pl.interpreter.parser.ast.BooleanExpression;
import pl.interpreter.parser.ast.BooleanLiteral;
import pl.interpreter.parser.ast.CompoundStatement;
import pl.interpreter.parser.ast.Expression;
import pl.interpreter.parser.ast.FloatConst;
import pl.interpreter.parser.ast.FunctionCall;
import pl.interpreter.parser.ast.FunctionDefinition;
import pl.interpreter.parser.ast.FunctionReturnType;
import pl.interpreter.parser.ast.FunctionSignature;
import pl.interpreter.parser.ast.IdentifierStatement;
import pl.interpreter.parser.ast.IdentifierStatementApplier;
import pl.interpreter.parser.ast.IdentifierWithValue;
import pl.interpreter.parser.ast.If;
import pl.interpreter.parser.ast.Initialization;
import pl.interpreter.parser.ast.InplaceValue;
import pl.interpreter.parser.ast.IntConst;
import pl.interpreter.parser.ast.Match;
import pl.interpreter.parser.ast.MatchBranch;
import pl.interpreter.parser.ast.MultiplicativeOperator;
import pl.interpreter.parser.ast.Node;
import pl.interpreter.parser.ast.ParameterSignature;
import pl.interpreter.parser.ast.Parentheses;
import pl.interpreter.parser.ast.PrimitiveInitialization;
import pl.interpreter.parser.ast.Program;
import pl.interpreter.parser.ast.Relation;
import pl.interpreter.parser.ast.Return;
import pl.interpreter.parser.ast.SingleStatement;
import pl.interpreter.parser.ast.StringLiteral;
import pl.interpreter.parser.ast.StructureDefinition;
import pl.interpreter.parser.ast.Subcondition;
import pl.interpreter.parser.ast.Term;
import pl.interpreter.parser.ast.UserTypeInitialization;
import pl.interpreter.parser.ast.Value;
import pl.interpreter.parser.ast.ValueAssignment;
import pl.interpreter.parser.ast.VarInitialization;
import pl.interpreter.parser.ast.VariableAssignment;
import pl.interpreter.parser.ast.VariantDefinition;
import pl.interpreter.parser.ast.While;

public class PrintTreeVisitor implements NodeVisitor {

    public final Writer writer;

    private final static String BRANCH_SYMBOL = "|";
    private final static String NAME_PREFIX = "-";
    private final static String DEPTH_SYMBOL = " ";
    private final static int NON_DEPTH_PREFIX_LENGTH = NAME_PREFIX.length() + BRANCH_SYMBOL.length();
    private int depth = 0;

    private final static String ID_MSG = "id";
    private final static String TYPE_MSG = "type";
    private final static String USER_TYPE_MSG = "userType";
    private final static String OPTIONAL_MISSING_MSG = "#";
    private final static String VALUE_MSG = "value";

    public PrintTreeVisitor(@NonNull Writer writer) {
        this.writer = writer;
    }

    @Override
    public void visit(Program program) {
        printNode(program);
        program.definitions().forEach(d -> {
            diveIn();
            if (d instanceof FunctionDefinition fd) {
                visit(fd);
            } else if (d instanceof StructureDefinition sd) {
                visit(sd);
            } else if (d instanceof VariantDefinition vd) {
                visit(vd);
            }
            diveOut();
        });
    }

    @Override
    public void visit(AdditiveOperator additiveOperator) {

    }

    @Override
    public void visit(While aWhile) {

    }

    @Override
    public void visit(AfterIdentifierStatement afterIdentifierStatement) {

    }

    @Override
    public void visit(As as) {

    }

    @Override
    public void visit(Block block) {
        printNode(block, Map.of(), block.row(), block.col());
        diveIn();
        block.statements().forEach(s -> {
            if (s instanceof SingleStatement ss) {
                this.visit(ss);
            } else if (s instanceof CompoundStatement cs) {
                this.visit(cs);
            } else if (s instanceof Block b) {
                this.visit(b);
            }
        });
        diveOut();
    }

    @Override
    public void visit(BooleanExpression booleanExpression) {

    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
        printNode(booleanLiteral,
                Map.of(VALUE_MSG, String.valueOf(booleanLiteral.value())),
                booleanLiteral.row(),
                booleanLiteral.col());
    }

    @Override
    public void visit(FloatConst floatConst) {

    }

    @Override
    public void visit(FunctionCall functionCall) {
        printNode(functionCall, Map.of(), functionCall.row(), functionCall.col());
        diveIn();
        printNode("FunctionArguments");
        diveIn();
        functionCall.values().forEach(this::visit);
        diveOut();
        diveOut();
    }

    @Override
    public void visit(FunctionDefinition functionDefinition) {
        printNode(functionDefinition, Map.of(), functionDefinition.row(), functionDefinition.col());
        diveIn();
        visit(functionDefinition.functionSignature());
        printNode("FunctionParameters");
        diveIn();
        functionDefinition.functionParameters().forEach(this::visit);
        diveOut();
        visit(functionDefinition.block());
        diveOut();
    }

    @Override
    public void visit(FunctionReturnType functionReturnType) {
        printNode(functionReturnType,
                Map.of(TYPE_MSG, functionReturnType.type().toString(),
                       USER_TYPE_MSG, getOptionalParamStr(functionReturnType.userType())),
                functionReturnType.row(),
                functionReturnType.col());
    }

    @Override
    public void visit(FunctionSignature functionSignature) {
        printNode(functionSignature,
                Map.of(ID_MSG, functionSignature.identifier()),
                functionSignature.row(),
                functionSignature.col());
        diveIn();
        visit(functionSignature.returnType());
        diveOut();
    }

    @Override
    public void visit(IdentifierStatement identifierStatement) {
        printNode(identifierStatement,
                Map.of(
                        ID_MSG, identifierStatement.identifier()
                ),
                identifierStatement.row(),
                identifierStatement.col());
        diveIn();
        visit(identifierStatement.applier());
        diveOut();
    }

    @Override
    public void visit(IdentifierWithValue identifierWithValue) {

    }

    @Override
    public void visit(If anIf) {

    }

    @Override
    public void visit(IntConst intConst) {

    }

    @Override
    public void visit(Match match) {

    }

    @Override
    public void visit(MatchBranch matchBranch) {

    }

    @Override
    public void visit(MultiplicativeOperator multiplicativeOperator) {

    }

    @Override
    public void visit(ParameterSignature parameterSignature) {
        printNode(parameterSignature,
                Map.of(
                        TYPE_MSG, parameterSignature.type().toString(),
                        ID_MSG, parameterSignature.identifier(),
                        USER_TYPE_MSG, getOptionalParamStr(parameterSignature.userType())
                ),
                parameterSignature.row(),
                parameterSignature.col());
    }

    @Override
    public void visit(Parentheses parentheses) {

    }

    @Override
    public void visit(PrimitiveInitialization primitiveInitialization) {
        printNode(primitiveInitialization,
                Map.of(
                        TYPE_MSG, primitiveInitialization.type().toString(),
                        ID_MSG, primitiveInitialization.identifier()
                ), primitiveInitialization.row(), primitiveInitialization.col());
    }

    @Override
    public void visit(Relation relation) {

    }

    @Override
    public void visit(Return returnStatement) {
        printNode(returnStatement,
                Map.of(),
                returnStatement.row(), returnStatement.col());
        if (returnStatement.value().isPresent()) {
            diveIn();
            this.visit(returnStatement.value().get());
            diveOut();
        }
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        printNode(stringLiteral,
                Map.of(
                        VALUE_MSG, stringLiteral.value()
                ),
                stringLiteral.row(), stringLiteral.col());
    }

    @Override
    public void visit(StructureDefinition structureDefinition) {

    }

    @Override
    public void visit(Subcondition subcondition) {

    }

    @Override
    public void visit(Term term) {

    }

    @Override
    public void visit(UserTypeInitialization userTypeInitialization) {
        printNode(userTypeInitialization,
                Map.of(
                        USER_TYPE_MSG, userTypeInitialization.typeIdentifier(),
                        ID_MSG, userTypeInitialization.identifier()
                ), userTypeInitialization.row(), userTypeInitialization.col());
    }

    @Override
    public void visit(ValueAssignment valueAssignment) {
        printNode(valueAssignment, Map.of(), valueAssignment.row(), valueAssignment.col());
        diveIn();
        visit(valueAssignment.value());
        diveOut();
    }

    @Override
    public void visit(VariableAssignment variableAssignment) {
        printNode(variableAssignment, Map.of(
                ID_MSG, variableAssignment.identifier()
        ), variableAssignment.row(), variableAssignment.col());
        diveIn();
        this.visit(variableAssignment.valueAssigned());
        diveOut();
    }

    @Override
    public void visit(VariantDefinition variantDefinition) {

    }

    @Override
    public void visit(VarInitialization varInitialization) {
        printNode(varInitialization, Map.of(), varInitialization.row(), varInitialization.col());
        diveIn();
        this.visit(varInitialization.initialization());
        diveOut();
    }

    @Override
    public void visit(Expression expression) {
        printNode(expression, Map.of(), expression.row(), expression.col());
    }

    @Override
    public void visit(SingleStatement singleStatement) {
        if (singleStatement instanceof IdentifierStatement is) {
            this.visit(is);
        } else if (singleStatement instanceof VarInitialization vi) {
            this.visit(vi);
        } else if (singleStatement instanceof PrimitiveInitialization pi) {
            this.visit(pi);
        } else if (singleStatement instanceof Return r) {
            this.visit(r);
        }
    }

    @Override
    public void visit(CompoundStatement compoundStatement) {

    }

    @Override
    public void visit(IdentifierStatementApplier identifierStatementApplier) {
        if (identifierStatementApplier instanceof ValueAssignment va) {
            this.visit(va);
        } else if (identifierStatementApplier instanceof FunctionCall fc) {
            this.visit(fc);
        } else if (identifierStatementApplier instanceof VariableAssignment va) {
            this.visit(va);
        }
    }

    @Override
    public void visit(Value value) {
        if (value instanceof Expression e) {
            this.visit(e);
        } else if (value instanceof InplaceValue iv) {
            this.visit(iv);
        }
    }

    @Override
    public void visit(InplaceValue inplaceValue) {
        if (inplaceValue instanceof StringLiteral sl) {
            this.visit(sl);
        } else if (inplaceValue instanceof BooleanLiteral bl) {
            this.visit(bl);
        }
    }

    @Override
    public void visit(Initialization initialization) {
        if (initialization instanceof PrimitiveInitialization pi) {
            this.visit(pi);
        } else if (initialization instanceof UserTypeInitialization ui) {
            this.visit(ui);
        }
    }

    private String getOptionalParamStr(Optional<? extends String> optional) {
        return optional.isPresent() ? optional.get() : OPTIONAL_MISSING_MSG;
    }

    private String getPrefix() {
        if (depth - NON_DEPTH_PREFIX_LENGTH < 0) {
            return "";
        }
        return DEPTH_SYMBOL.repeat(depth - NON_DEPTH_PREFIX_LENGTH) +
                BRANCH_SYMBOL +
                NAME_PREFIX;
    }

    private void printNode(Node node) {
        write(getPrefix() + node.getClass().getSimpleName().concat("\n"));
    }

    private void printNode(String nodeName) {
        write(getPrefix() + nodeName.concat("\n"));
    }

    private void printNode(Node node, Map<String, String> params, int row, int col) {
        var str = getPrefix() +
                node.getClass().getSimpleName() +
                ' ' +
                "<row: " +
                row +
                ", col: " +
                col +
                "> " +
                getParametersString(params) +
                '\n';
        write(str);
    }

    private String getParametersString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> getParamString(e.getKey(), e.getValue()))
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
