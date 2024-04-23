package pl.interpreter.parser.ast;

import java.util.List;
import pl.interpreter.parser.NodeVisitor;

public record FunctionDefinition(FunctionSignature functionSignature, List<ParameterSignature> functionParameters, Block block, int row, int col)
        implements Definition {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
