package pl.interpreter.parser;

import java.util.List;
import pl.interpreter.TokenType;

public class TokenTypeGroupsProvider {
    public static final List<TokenType> VAR_TYPES = List.of(TokenType.KW_STRING, TokenType.KW_INT, TokenType.KW_FLOAT, TokenType.KW_BOOL);
    public static final List<TokenType> VALUE_TYPES =
            List.of(TokenType.STRING_CONST, TokenType.INT_CONST, TokenType.FLOAT_CONST, TokenType.KW_TRUE, TokenType.KW_FALSE);
    public static final List<TokenType> BOOL_TYPES = List.of(TokenType.KW_TRUE, TokenType.KW_FALSE);
    public static final List<TokenType> NUMBER_TYPES = List.of(TokenType.INT_CONST, TokenType.FLOAT_CONST);
    private TokenTypeGroupsProvider() {
    }
}
