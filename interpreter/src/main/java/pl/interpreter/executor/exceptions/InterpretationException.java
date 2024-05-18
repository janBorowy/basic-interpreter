package pl.interpreter.executor.exceptions;

import lombok.Getter;
import pl.interpreter.parser.Position;

public class InterpretationException extends RuntimeException {

    @Getter
    private final Position position;

    public InterpretationException(String message) {
        this(message, new Position(0, 0));
    }

    public InterpretationException(String message, Position position) {
        super(message);
        this.position = position;
    }
}
