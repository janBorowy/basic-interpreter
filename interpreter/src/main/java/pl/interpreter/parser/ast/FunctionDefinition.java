package pl.interpreter.parser.ast;

public class FunctionDefinition implements Node {

    private final FunctionSignature functionSignature;
    private final FunctionParameters functionParameters;

    public FunctionDefinition(FunctionSignature functionSignature, FunctionParameters functionParameters) {
        this.functionSignature = functionSignature;
        this.functionParameters = functionParameters;
    }
}
