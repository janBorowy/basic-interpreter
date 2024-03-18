package pl.interpreter;

import java.util.regex.Pattern;

public class TokenPatternsProvider {

    private TokenPatternsProvider() {}

    public static Pattern getIdentifierPattern() {
        return Pattern.compile("[1-9a-zA-Z_]+");
    }

}
