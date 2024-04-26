package pl.interpreter;

public class UnknownNodeException extends RuntimeException {

    public static final String INTERFACE_NOT_MATCHED_EXCEPTION = "Unknown implementation";

    public UnknownNodeException() {
        super(INTERFACE_NOT_MATCHED_EXCEPTION);
    }

    public UnknownNodeException(String message) {
        super(message);
    }
}
