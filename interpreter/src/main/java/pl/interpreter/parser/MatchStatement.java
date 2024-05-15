package pl.interpreter.parser;

import java.util.List;
import lombok.Getter;

@Getter
public class MatchStatement extends Statement implements Instruction {

    private final Value expression;
    private final List<MatchBranch> branches;

    public MatchStatement(Value expression, List<MatchBranch> branches, Position tokenPosition) {
        super(tokenPosition);
        this.expression = expression;
        this.branches = branches;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
