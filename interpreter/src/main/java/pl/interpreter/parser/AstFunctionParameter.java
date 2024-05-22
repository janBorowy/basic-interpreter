package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class AstFunctionParameter extends Statement {

    private final ParameterType type;
    private final String id;
    private final boolean var;

    public AstFunctionParameter(ParameterType type, String id, boolean var, Position position) {
        super(position);
        this.type = type;
        this.id = id;
        this.var = var;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
