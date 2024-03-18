package pl.interpreter;

import java.io.Reader;
import lombok.NonNull;

public class LexicalAnalyzer {


    final Reader reader;

    public LexicalAnalyzer(@NonNull Reader reader) {
        this.reader = reader;
    }

    public String getToken() {
        return "a";
    }
}
