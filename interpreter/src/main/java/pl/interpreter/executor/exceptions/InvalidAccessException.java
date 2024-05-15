package pl.interpreter.executor.exceptions;

public class InvalidAccessException extends RuntimeException {

    public InvalidAccessException(String message) {
        super(message);
    }
}
