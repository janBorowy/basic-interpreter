package pl.interpreter.executor.exceptions;

public class FunctionCallException extends RuntimeException {

    public FunctionCallException(String message) {
        super(message);
    }
}
