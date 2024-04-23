package pl.interpreter.parser.ast;

import java.util.List;
import pl.interpreter.parser.NodeVisitor;

public record StructureDefinition(String identifier, List<ParameterSignature> parameterSignatures, int row, int col) implements Definition {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
