package pl.interpreter.parser;

public interface ExpressionVisitor {
    void visit(Value expression);

    void visit(Alternative alternative);

    void visit(BooleanLiteral booleanLiteral);

    void visit(Cast cast);

    void visit(Conjunction conjunction);

    void visit(DotAccess dotAccess);

    void visit(FloatLiteral floatLiteral);

    void visit(FunctionCall functionCall);

    void visit(Identifier identifier);

    void visit(IntLiteral intLiteral);

    void visit(Multiplication multiplication);

    void visit(Negation negation);

    void visit(Relation relation);

    void visit(StringLiteral stringLiteral);

    void visit(Sum sum);
}
