package pl.interpreter.executor.exceptions;

public class InvalidValueTypeException extends RuntimeException {
    public InvalidValueTypeException(String message) {
        super(message);
    }
}
