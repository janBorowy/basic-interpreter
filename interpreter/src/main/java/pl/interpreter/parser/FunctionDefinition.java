package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class FunctionDefinition extends Statement implements Definition {

    private final FunctionReturnType returnType;
    private final String id;
    private final ParameterSignatureMap parameters;
    private final Block block;

    public FunctionDefinition(FunctionReturnType returnType,
                              String id,
                              ParameterSignatureMap parameters,
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
