package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class Initialization extends Statement implements Instruction {

    private final String id;
    private final String userType;
    private final VariableType type;
    private final Value expression;
    private final boolean var;

    public Initialization(String id, String userType, VariableType type, boolean var, Value expression, Position tokenPosition) {
        super(tokenPosition);
        this.id = id;
        this.type = type;
        this.var = var;
        this.userType = userType;
        this.expression = expression;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
