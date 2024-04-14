package pl.interpreter.parser.ast;

public class VariableType implements Node {

    private final VariableTypeEnum type;

    public VariableType(VariableTypeEnum type) {
        this.type = type;
    }
}
