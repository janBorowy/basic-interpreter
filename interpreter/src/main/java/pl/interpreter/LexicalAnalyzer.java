package pl.interpreter;

import java.io.InputStream;

public class LexicalAnalyzer {

    final InputStream inputStream;

    public LexicalAnalyzer(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getToken() {
        return "a";
    }
}
