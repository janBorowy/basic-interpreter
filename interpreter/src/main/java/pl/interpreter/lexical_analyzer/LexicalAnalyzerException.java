package pl.interpreter.lexical_analyzer;

import lombok.Getter;

@Getter
public class LexicalAnalyzerException extends RuntimeException {

    private final int errorRow;
    private final int errorCol;
    public LexicalAnalyzerException(String message, int errorRow, int errorCol) {
        super(message);
        this.errorRow = errorRow;
        this.errorCol = errorCol;
    }

}
