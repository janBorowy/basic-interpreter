package pl.interpreter;

import java.util.regex.Pattern;

public class TokenPatternsProvider {

    private TokenPatternsProvider() {}

    public static Pattern getIdentifierPattern() {
        // TODO: fix dangerous regex
        return Pattern.compile("[a-zA-Z_]\\w*");
    }

    public static Pattern getNumberPattern() {
        return Pattern.compile("-?\\d+(\\.\\d+)?");
    }

}
