package pl.interpreter.parser.ast;

public class UserType implements Node {

    private final String identifier;

    public UserType(String identifier) {
        this.identifier = identifier;
    }
}
