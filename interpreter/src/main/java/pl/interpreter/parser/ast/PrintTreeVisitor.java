package pl.interpreter.parser.ast;

import java.util.Map;
import pl.interpreter.parser.NodeVisitor;

public class PrintTreeVisitor implements NodeVisitor {

    private final static String DEPTH_INDICATOR = "    ";
    private int depth = 0;

    @Override
    public void visit(Program program) {
        print(program, Map.of());
        ++depth;
        program.definitions().forEach(d -> {
                if (d instanceof FunctionDefinition fd) {
                    visit(fd);
                } else if (d instanceof StructureDefinition sd) {
                    visit(sd);
                } else if (d instanceof VariantDefinition vd) {
                    visit(vd);
                }
            }
        );
        --depth;
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
        print(block);
    }

    @Override
    public void visit(BoolConst boolConst) {

    }

    @Override
    public void visit(BooleanExpression booleanExpression) {

    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {

    }

    @Override
    public void visit(FloatConst floatConst) {

    }

    @Override
    public void visit(FunctionCall functionCall) {

    }

    @Override
    public void visit(FunctionDefinition functionDefinition) {
        print(functionDefinition, Map.of());
        goInward();
        visit(functionDefinition.functionSignature());
        functionDefinition.functionParameters().forEach(this::visit);
        visit(functionDefinition.block());
        goOutword();
    }

    @Override
    public void visit(FunctionReturnType functionReturnType) {
        if (functionReturnType.userType().isPresent()) {
            print(functionReturnType, Map.of(
                    "type", functionReturnType.type().toString(),
                    "userType", functionReturnType.userType().get()));
        } else {
            print(functionReturnType, Map.of(
                    "type", functionReturnType.type().toString()));
        }
    }

    @Override
    public void visit(FunctionSignature functionSignature) {
        print(functionSignature, Map.of("identifier", functionSignature.identifier()));
        visit(functionSignature.returnType());
    }

    @Override
    public void visit(IdentifierStatement identifierStatement) {

    }

    @Override
    public void visit(IdentifierWithValue identifierWithValue) {

    }

    @Override
    public void visit(If anIf) {

    }

    @Override
    public void visit(InitializationSignature initializationSignature) {

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
        if (parameterSignature.userType().isPresent()) {
            print(parameterSignature, Map.of(
                    "type", parameterSignature.type().toString(),
                    "identifier", parameterSignature.identifier(),
                    "userType", parameterSignature.userType().get()));
        } else {
            print(parameterSignature, Map.of(
                    "type", parameterSignature.type().toString(),
                    "identifier", parameterSignature.identifier()));
        }
    }

    @Override
    public void visit(Parentheses parentheses) {

    }

    @Override
    public void visit(PrimitiveInitialization primitiveInitialization) {

    }

    @Override
    public void visit(Relation relation) {

    }

    @Override
    public void visit(Return aReturn) {

    }

    @Override
    public void visit(StringConst stringConst) {

    }

    @Override
    public void visit(StringLiteral stringLiteral) {

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
    public void visit(UserType userType) {

    }

    @Override
    public void visit(UserTypeInitialization userTypeInitialization) {

    }

    @Override
    public void visit(ValueAssignment valueAssignment) {

    }

    @Override
    public void visit(VariableAssignment variableAssignment) {

    }

    @Override
    public void visit(VariantDefinition variantDefinition) {

    }

    @Override
    public void visit(VarInitialization varInitialization) {

    }

    @Override
    public void visit(VoidType voidType) {

    }

    @Override
    public void visit(Expression expression) {

    }

    private void print(Node node) {
        print(node, Map.of());
    }

    private void print(Node node, Map<String, String> params) {
        var builder = new StringBuilder()
                .append(DEPTH_INDICATOR.repeat(depth))
                .append(node.getClass().getSimpleName())
                .append(" ");
        params.entrySet().stream()
                .map(e -> getParamString(e.getKey(), e.getValue()))
                .forEach(builder::append);
        System.out.println(builder);
    }

    private String getParamString(String name, String value) {
        return name + "=" + value;
    }

    private void goInward() {
        ++depth;
    }

    private void goOutword() {
        --depth;
    }
}
