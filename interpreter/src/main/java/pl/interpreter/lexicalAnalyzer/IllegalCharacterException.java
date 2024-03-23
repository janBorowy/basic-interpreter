package pl.interpreter.lexicalAnalyzer;

public class IllegalCharacterException extends RuntimeException{

    public IllegalCharacterException() {
        super("Illegal character found");
    }
}
