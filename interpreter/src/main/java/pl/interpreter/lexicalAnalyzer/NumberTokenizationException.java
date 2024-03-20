package pl.interpreter.lexicalAnalyzer;

public class NumberTokenizationException extends RuntimeException {
    public NumberTokenizationException() {
        super("Failed to tokenize a number");
    }
}
