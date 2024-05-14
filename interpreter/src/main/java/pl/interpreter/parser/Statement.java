package pl.interpreter.parser;

import lombok.Getter;

public abstract class Statement {

    @Getter
    private final Position position;

    public Statement(Position tokenPosition) {
        this.position = tokenPosition;
    }

    public abstract void accept(StatementVisitor visitor);
}
