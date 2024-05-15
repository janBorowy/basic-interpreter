package pl.interpreter.parser;

import java.util.List;
import lombok.Getter;

@Getter
public class StructureDefinition extends Statement implements Definition {

    private final String id;
    private final List<Parameter> parameters;

    public StructureDefinition(String id, List<Parameter> parameters, Position tokenPosition) {
        super(tokenPosition);
        this.id = id;
        this.parameters = parameters;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
