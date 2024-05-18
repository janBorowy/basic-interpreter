package pl.interpreter.parser;

import lombok.Getter;

@Getter
public class ParserException extends RuntimeException {

    private int errorCol;
    private int errorRow;

    public ParserException(String message, int row, int col) {
        super(message);
        this.errorRow = row;
        this.errorCol = col;
    }
}
