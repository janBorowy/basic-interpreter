package pl.interpreter.parser.ast;

import java.util.List;
import pl.interpreter.parser.NodeVisitor;

public record Program(List<Definition> definitions) implements Node {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
