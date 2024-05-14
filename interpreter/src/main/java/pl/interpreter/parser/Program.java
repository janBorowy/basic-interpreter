package pl.interpreter.parser;

import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class Program extends Statement {

    private final Map<String, Definition> definitions;

    public Program(Map<String, Definition> definitions, Position tokenPosition) {
        super(tokenPosition);
        this.definitions = definitions;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
