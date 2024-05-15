package pl.interpreter.executor.exceptions;

public class InvalidParameterTypeException extends RuntimeException {

    public InvalidParameterTypeException(String message) {
        super(message);
    }
}
