package pl.interpreter.lexical_analyzer;

public class InterpreterIOException extends RuntimeException {

    public InterpreterIOException(String ioExceptionMessage) {
        super("IOException occured: " + ioExceptionMessage);
    }
}
