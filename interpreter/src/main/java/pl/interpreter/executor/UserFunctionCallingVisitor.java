package pl.interpreter.executor;

import lombok.Getter;
import pl.interpreter.parser.Assignment;
import pl.interpreter.parser.Block;
import pl.interpreter.parser.FunctionCall;
import pl.interpreter.parser.FunctionVisitor;
import pl.interpreter.parser.IfStatement;
import pl.interpreter.parser.Initialization;
import pl.interpreter.parser.MatchStatement;
import pl.interpreter.parser.ReturnStatement;
import pl.interpreter.parser.WhileStatement;

public class UserFunctionCallingVisitor implements FunctionVisitor {

    Environment environment;
    @Getter
    Value returnedValue;

    public UserFunctionCallingVisitor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void visit(Block block) {

    }

    @Override
    public void visit(FunctionCall functionCall) {

    }

    @Override
    public void visit(Assignment assignment) {

    }

    @Override
    public void visit(Initialization initialization) {

    }

    @Override
    public void visit(ReturnStatement Statement) {

    }

    @Override
    public void visit(IfStatement statement) {

    }

    @Override
    public void visit(WhileStatement statement) {

    }

    @Override
    public void visit(MatchStatement statement) {

    }
}
