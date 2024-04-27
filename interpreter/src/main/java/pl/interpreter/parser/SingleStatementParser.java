package pl.interpreter.parser;

import java.util.Optional;
import pl.interpreter.TokenType;

public class SingleStatementParser extends Parser {

    private final ExpressionParser expressionParser;

    public SingleStatementParser(ExpressionParser conditionParser, TokenManager tokenManager) {
        super(tokenManager);
        this.expressionParser = conditionParser;
    }

    // singleStatement ::= (identifierStatement
    //                           | "var" initialization // var initialization
    //                           | return) ";";
    public Optional<Statement> parseSingleStatement() {
        var statement = parseReturn();
        if (statement.isEmpty()) {
            return Optional.empty();
        }
        mustBe(TokenType.SEMICOLON);
        return Optional.of(statement.get());
    }


    // return ::= "return", [conditionalExpression];
    public Optional<ReturnStatement> parseReturn() {
        var position = getTokenPosition();
        if (!tokenIsOfType(TokenType.KW_RETURN)) {
            return Optional.empty();
        }
        consumeToken();
        var expression = expressionParser.parseExpression();
        if (expression.isEmpty()) {
            return Optional.of(new ReturnStatement(null, position));
        }
        return Optional.of(new ReturnStatement(expression.get(), position));
    }
}
