package pl.interpreter.parser.ast;

public class ParameterSignature implements Node {

    private final Node type;
    private final String identifier;

    public ParameterSignature(Node type, String identifier) {
        this.type = type;
        this.identifier = identifier;
    }
}
