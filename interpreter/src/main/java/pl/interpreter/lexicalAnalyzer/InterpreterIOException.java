package pl.interpreter.lexicalAnalyzer;

public class InterpreterIOException extends RuntimeException {

    public InterpreterIOException(String ioExceptionMessage) {
        super("IOException occured: " + ioExceptionMessage);
    }
}
