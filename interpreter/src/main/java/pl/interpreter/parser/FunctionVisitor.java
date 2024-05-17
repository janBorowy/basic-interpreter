package pl.interpreter.parser;

public interface FunctionVisitor {
    void visit(Block block);

    void visit(FunctionCall functionCall);

    void visit(Assignment assignment);

    void visit(Initialization initialization);

    void visit(ReturnStatement statement);

    void visit(IfStatement statement);

    void visit(WhileStatement statement);

    void visit(MatchStatement statement);

    void visit(Instruction instruction);
}
