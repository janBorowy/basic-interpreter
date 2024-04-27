package pl.interpreter.parser;

public interface StatementVisitor {

    void visit(ReturnStatement returnStatement);

    void visit(Alternative alternative);

    void visit(Cast cast);

    void visit(Conjunction conjunction);

    void visit(Relation relation);

    void visit(Expression expression);

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
}
