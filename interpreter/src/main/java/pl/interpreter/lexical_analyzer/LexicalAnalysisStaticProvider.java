package pl.interpreter.lexical_analyzer;

import java.util.HashMap;
import java.util.Map;
import pl.interpreter.TokenType;

public class LexicalAnalysisStaticProvider {

    private static Map<String, TokenType> keywords;
    private LexicalAnalysisStaticProvider() {}

    public static Map<String, TokenType> getKeywords() {
        if (keywords == null) {
            keywords = new HashMap<>();
            prepareKeywords();
        }
        return keywords;
    }

    private static void prepareKeywords() {
        keywords.put("struct", TokenType.KW_STRUCT);
        keywords.put("variant", TokenType.KW_VARIANT);
        keywords.put("var", TokenType.KW_VAR);
        keywords.put("return", TokenType.KW_RETURN);
        keywords.put("while", TokenType.KW_WHILE);
        keywords.put("match", TokenType.KW_MATCH);
        keywords.put("if", TokenType.KW_IF);
        keywords.put("else", TokenType.KW_ELSE);
        keywords.put("as", TokenType.KW_AS);
        keywords.put("void", TokenType.KW_VOID);
        keywords.put("int", TokenType.KW_INT);
        keywords.put("float", TokenType.KW_FLOAT);
        keywords.put("string", TokenType.KW_STRING);
        keywords.put("bool", TokenType.KW_BOOL);
        keywords.put("true", TokenType.KW_TRUE);
        keywords.put("false", TokenType.KW_FALSE);
        keywords.put("and", TokenType.KW_AND);
        keywords.put("or", TokenType.KW_OR);
        keywords.put("default", TokenType.KW_DEFAULT);
    }

}
