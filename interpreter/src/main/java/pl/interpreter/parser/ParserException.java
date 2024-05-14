package pl.interpreter.parser;

public class ParserException extends RuntimeException {

    public ParserException(String message, int row, int col) {
        super(message);
    }
}
