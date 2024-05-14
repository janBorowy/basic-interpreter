package pl.interpreter.executor.exceptions;

public class ExpressionEvaluationException extends RuntimeException {
    public ExpressionEvaluationException() {
        super("Unexpected error occurred during evaluation");
    }

    public ExpressionEvaluationException(String message) {
        super(message);
    }
}
