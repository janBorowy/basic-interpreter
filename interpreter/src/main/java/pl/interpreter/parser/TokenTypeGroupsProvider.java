package pl.interpreter.parser;

import java.util.List;
import pl.interpreter.TokenType;

public class TokenTypeGroupsProvider {
    public static final List<TokenType> VAR_TYPES = List.of(TokenType.KW_STRING, TokenType.KW_INT, TokenType.KW_FLOAT, TokenType.KW_BOOL);

    private TokenTypeGroupsProvider() {}
}
