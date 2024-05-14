package pl.interpreter.parser;

import java.util.List;
import lombok.Getter;

@Getter
public class Program extends Statement {

    private final List<Definition> definitions;

    public Program(List<Definition> definitions, Position tokenPosition) {
        super(tokenPosition);
        this.definitions = definitions;
    }

    @Override
    public void accept(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
