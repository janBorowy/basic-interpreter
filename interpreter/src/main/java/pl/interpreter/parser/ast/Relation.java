package pl.interpreter.parser.ast;

import java.util.Optional;
import pl.interpreter.parser.NodeVisitor;

public record Relation(Expression first, Optional<ArithmeticCondition> arithmeticCondition, Optional<Expression> second, int row, int col) implements LogicTerm {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
