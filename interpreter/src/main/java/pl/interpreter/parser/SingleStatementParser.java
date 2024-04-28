package pl.interpreter.parser;

import java.util.ArrayList;
import java.util.Optional;
import pl.interpreter.TokenType;

public class SingleStatementParser extends Parser {

    private final ExpressionParser expressionParser;

    public SingleStatementParser(ExpressionParser conditionParser, TokenManager tokenManager) {
        super(tokenManager);
        this.expressionParser = conditionParser;
    }

    // singleStatement ::= (identifierStatement
    //                   | primitiveInitialization
    //                   | return) ";";
    public Optional<Instruction> parseSingleStatement() {
        var statement = parseReturn()
                .or(this::parseIdentifierStatement)
                .or(this::parsePrimitiveInitialization);
        if (statement.isEmpty()) {
            return Optional.empty();
        }
        mustBe(TokenType.SEMICOLON);
        consumeToken();
        return Optional.of(statement.get());
    }

    // identifierStatement ::= identifier (arguments // function call
    //                       | "=" expression // assignment
    //                       | identifier "=" expression) // user type initialization
    public Optional<Instruction> parseIdentifierStatement() {
        if (!tokenIsOfType(TokenType.IDENTIFIER)) {
            return Optional.empty();
        }
        var id = (String) token().value();
        consumeToken();
        return parseFunctionCall(id)
                .or(() -> parseAssignment(id))
                .or(() -> parseInitialization(id));
    }

    // functionCall ::= identifier, arguments;
    // arguments    ::= "(", [ expression {"," expression } ], ")";
    public Optional<Instruction> parseFunctionCall(String id) {
        var position = getTokenPosition();
        if (!tokenIsOfType(TokenType.LEFT_PARENTHESES)) {
            return Optional.empty();
        }
        consumeToken();
        var arguments = new ArrayList<Expression>();
        var expression = expressionParser.parseExpression();
        expression.ifPresent(arguments::add);
        while(tokenIsOfType(TokenType.COMMA)) {
            consumeToken();
            expression = expressionParser.parseExpression();
            if (expression.isEmpty()) {
                throwParserError("Expected expression");
            }
            arguments.add(expression.get());
        }
        mustBe(TokenType.RIGHT_PARENTHESES);
        consumeToken();
        return Optional.of(new FunctionCall(id, arguments, position));
    }

    // "=" expression
    private Optional<Assignment> parseAssignment(String id) {
        var position = getTokenPosition();
        if (!tokenIsOfType(TokenType.ASSIGNMENT)) {
            return Optional.empty();
        }
        consumeToken();
        var expression = expressionParser.parseExpression();
        if (expression.isEmpty()) {
            throwParserError("Expected expression");
        }
        return Optional.of(new Assignment(id, expression.get(), position));
    }

    // primitiveInitialization ::= primitiveType identifier "=" expression;
    private Optional<Initialization> parsePrimitiveInitialization() {
        var position = getTokenPosition();
        var type = VariableType.parseVariableType(token());
        if (type.isEmpty() || type.get() == VariableType.USER_TYPE) {
            return Optional.empty();
        }
        consumeToken();
        var id = parseMustBeIdentifier();
        mustBe(TokenType.ASSIGNMENT);
        consumeToken();
        var expression = expressionParser.parseExpression();
        if (expression.isEmpty()) {
            throwParserError("Expected expression");
        }
        return Optional.of(new Initialization(id, null, type.get(), false, expression.get(), position));
    }

    // identifier "=" expression
    private Optional<Initialization> parseInitialization(String userType) {
        var position = getTokenPosition();
        if (!tokenIsOfType(TokenType.IDENTIFIER)) {
            return Optional.empty();
        }
        var id = (String) token().value();
        consumeToken();
        mustBe(TokenType.ASSIGNMENT);
        consumeToken();
        var expression = expressionParser.parseExpression();
        if (expression.isEmpty()) {
            throwParserError("Expected expression");
        }
        return Optional.of(new Initialization(id, userType, VariableType.USER_TYPE, false, expression.get(), position));
    }

    // return ::= "return", [expression];
    public Optional<Instruction> parseReturn() {
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
