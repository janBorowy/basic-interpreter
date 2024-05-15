package pl.interpreter.executor.exceptions;

public class InvalidFunctionCallException extends RuntimeException {

    public InvalidFunctionCallException(String message) {
        super(message);
    }
}
