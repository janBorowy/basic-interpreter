package pl.interpreter.parser;

import java.util.List;
import lombok.Getter;

@Getter
public class FunctionDefinition extends Statement implements Definition {

    private final FunctionReturnType returnType;
    private final String id;
    private final List<Parameter> parameters;
    private final Block block;

    public FunctionDefinition(FunctionReturnType returnType,
                              String id,
                              List<Parameter> parameters,
                              Block block,
                              Position tokenPosition) {
        super(tokenPosition);
        this.returnType = returnType;
        this.id = id;
        this.parameters = parameters;
        this.block = block;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
