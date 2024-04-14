package pl.interpreter.parser.ast;

public class FunctionSignature implements Node {

    private final Node returnType;
    private final String identifier;

    public FunctionSignature(Node returnType, String identifier) {
        this.returnType = returnType;
        this.identifier = identifier;
    }
}
