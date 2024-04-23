package pl.interpreter.parser.ast;

import java.util.List;
import pl.interpreter.parser.NodeVisitor;

public record Match(IdentifierWithValue identifierValue, List<MatchBranch> branches, int row, int col) implements CompoundStatement {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
