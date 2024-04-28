package pl.interpreter.parser;

import java.util.List;
import lombok.Getter;

@Getter
public class VariantDefinition extends Statement implements Definition {

    private final String id;
    private final List<String> structureIds;

    public VariantDefinition(String id, List<String> structureIds, Position tokenPosition) {
        super(tokenPosition);
        this.id = id;
        this.structureIds = structureIds;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
