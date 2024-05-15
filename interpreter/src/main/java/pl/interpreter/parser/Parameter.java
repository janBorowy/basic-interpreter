package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class Parameter extends Statement {

    private final ParameterType type;
    private final String id;

    public Parameter(ParameterType type, String id, Position position) {
        super(position);
        this.type = type;
        this.id = id;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
