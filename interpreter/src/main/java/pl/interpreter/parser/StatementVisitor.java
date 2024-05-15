package pl.interpreter.parser;

public interface StatementVisitor {

    void visit(ReturnStatement returnStatement);

    void visit(Alternative alternative);

    void visit(Cast cast);

    void visit(Conjunction conjunction);

    void visit(Relation relation);

    void visit(Value expression);

    void visit(Sum sum);

    void visit(BooleanLiteral booleanLiteral);

    void visit(FloatLiteral floatLiteral);

    void visit(IntLiteral intLiteral);

    void visit(Multiplication multiplication);

    void visit(Negation negation);

    void visit(StringLiteral stringLiteral);

    void visit(DotAccess dotAccess);

    void visit(Identifier identifier);

    void visit(FunctionCall functionCall);

    void visit(StructureDefinition structureDefinition);

    void visit(Program program);

    void visit(Definition definition);

    void visit(ParameterSignatureMap parameterSignatureMap);

    void visit(VariantDefinition variantDefinition);

    void visit(FunctionDefinition functionDefinition);

    void visit(Block block);

    void visit(Instruction instruction);

    void visit(Assignment assignment);

    void visit(Initialization initialization);

    void visit(IfStatement ifStatement);

    void visit(WhileStatement whileStatement);

    void visit(MatchStatement matchStatement);

    void visit(MatchBranch matchBranch);

    void visit(Parameter parameter);
}
