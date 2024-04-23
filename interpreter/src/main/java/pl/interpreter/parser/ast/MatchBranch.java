package pl.interpreter.parser.ast;

import pl.interpreter.parser.NodeVisitor;

public record MatchBranch(String type, String identifier, Instruction instruction, int row, int col) implements Node {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
