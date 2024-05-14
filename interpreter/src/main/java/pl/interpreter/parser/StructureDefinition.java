package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class StructureDefinition extends Statement implements Definition {

    private final String id;
    private final ParameterSignatureMap parameters;

    public StructureDefinition(String id, ParameterSignatureMap parameters, Position tokenPosition) {
        super(tokenPosition);
        this.id = id;
        this.parameters = parameters;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
