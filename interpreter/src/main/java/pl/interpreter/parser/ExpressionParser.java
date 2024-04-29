package pl.interpreter.parser;

import java.util.ArrayList;
import java.util.Optional;
import pl.interpreter.TokenType;

public class ExpressionParser extends Parser {

    public ExpressionParser(TokenManager tokenManager) {
        super(tokenManager);
    }

    // expression ::= alternative, {"and", alternative};
    public Optional<Expression> parseExpression() {
        var position = getTokenPosition();
        var left = parseAlternative();
        if (left.isEmpty()) {
            return Optional.empty();
        }
        while (tokenIsOfType(TokenType.KW_AND)) {
            consumeToken();
            var right = parseAlternative();
            if (right.isEmpty()) {
                throwParserError("Expected operand");
            }
            left = Optional.of(new Conjunction(left.get(), right.get(), position));
        }
        return left;
    }

    // alternative ::= relation, {"or", relation};
    private Optional<Expression> parseAlternative() {
        var position = getTokenPosition();
        var left = parseRelation();
        if (left.isEmpty()) {
            return Optional.empty();
        }
        while (tokenIsOfType(TokenType.KW_OR)) {
            consumeToken();
            var right = parseRelation();
            if (right.isEmpty()) {
                throwParserError("Expected operand");
            }
            left = Optional.of(new Alternative(left.get(), right.get(), position));
        }
        return left;
    }

    // relation ::= cast, [relationalOperator, cast];
    private Optional<Expression> parseRelation() {
        var position = getTokenPosition();
        var left = parseCast();
        if (left.isEmpty()) {
            return Optional.empty();
        }
        var operator = RelationalOperator.parseRelationalOperator(token());
        if (operator.isEmpty()) {
            return left;
        }
        consumeToken();
        var right = parseCast();
        if (right.isEmpty()) {
            throwParserError("Expected operand");
        }
        return Optional.of(new Relation(left.get(), operator.get(), right.get(), position));
    }

    // cast ::= sum, ["as", primitiveType]
    private Optional<Expression> parseCast() {
        var position = getTokenPosition();
        var sum = parseSum();
        if (sum.isEmpty()) {
            return Optional.empty();
        }
        if (tokenIsOfType(TokenType.KW_AS)) {
            consumeToken();
            var type = PrimitiveType.parsePrimitiveType(token());
            if (type.isEmpty()) {
                throwParserError("Expected primitive type");
            }
            consumeToken();
            return Optional.of(new Cast(sum.get(), type.get(), position));
        }
        return sum;
    }

    // sum ::= multiplication, {additionOperator, multiplication};
    private Optional<Expression> parseSum() {
        var position = getTokenPosition();
        var left = parseMultiplication();
        if (left.isEmpty()) {
            return Optional.empty();
        }
        var operator = AdditionOperator.parseAdditionOperator(token());
        while (operator.isPresent()) {
            consumeToken();
            var right = parseMultiplication();
            if (right.isEmpty()) {
                throwParserError("Expected expression");
            }
            left = Optional.of(new Sum(left.get(), operator.get(), right.get(), position));
            operator = AdditionOperator.parseAdditionOperator(token());
        }
        return left;
    }

    // multiplication ::= negation, {multiplicationOperator, negation};
    private Optional<Expression> parseMultiplication() {
        var position = getTokenPosition();
        var left = parseNegation();
        if (left.isEmpty()) {
            return Optional.empty();
        }
        var operator = MultiplicationOperator.parseMultiplicationOperator(token());
        while (operator.isPresent()) {
            consumeToken();
            var right = parseNegation();
            if (right.isEmpty()) {
                throwParserError("Expected expression");
            }
            left = Optional.of(new Multiplication(left.get(), operator.get(), right.get(), position));
            operator = MultiplicationOperator.parseMultiplicationOperator(token());
        }
        return left;
    }

    // negation ::= ["!"] factor;
    private Optional<Expression> parseNegation() {
        var position = getTokenPosition();
        if (tokenIsOfType(TokenType.NEGATION_OPERATOR)) {
            consumeToken();
            var factor = parseFactor();
            if (factor.isEmpty()) {
                throwParserError("Expected expression");
            }
            return Optional.of(new Negation(factor.get(), position));
        }
        return parseFactor();
    }

    // factor ::= identifierOrFunctionCall {"." identifier} // dot is access to structure field
    //          | number // integer or float literal
    //          | booleanLiteral
    //          | "(", expression, ")";
    private Optional<Expression> parseFactor() {
        return parseDotAccess()
                .or(this::parseNumber)
                .or(this::parseBooleanLiteral)
                .or(this::parseNestedExpression)
                .or(this::parseStringLiteral);
    }

    // dotAccess ::= identifierOrFunctionCall {"." identifier}
    public Optional<Expression> parseDotAccess() {
        var position = getTokenPosition();
        var expressionOptional = parseIdentifierOrFunctionCall();
        if (expressionOptional.isEmpty()) {
            return Optional.empty();
        }
        var expression = expressionOptional.get();
        while (tokenIsOfType(TokenType.DOT)) {
            consumeToken();
            var fieldId = parseMustBeIdentifier();
            expression = new DotAccess(expression, fieldId, position);
        }
        return Optional.of(expression);
    }

    private Optional<Expression> parseBooleanLiteral() {
        var position = getTokenPosition();
        if (tokenIsOfType(TokenType.KW_TRUE)) {
            consumeToken();
            return Optional.of(new BooleanLiteral(true, position));
        } else if (tokenIsOfType(TokenType.KW_FALSE)) {
            consumeToken();
            return Optional.of(new BooleanLiteral(false, position));
        }
        return Optional.empty();
    }

    private Optional<Expression> parseNumber() {
        var position = getTokenPosition();
        if (tokenIsOfType(TokenType.INT_CONST)) {
            var value = (int) token().value();
            consumeToken();
            return Optional.of(new IntLiteral(value, position));
        } else if (tokenIsOfType(TokenType.FLOAT_CONST)) {
            var value = (float) token().value();
            consumeToken();
            return Optional.of(new FloatLiteral(value, position));
        }
        return Optional.empty();
    }

    // identifierOrFunctionCall ::= identifier ["("[ expression {"," expression } ]")"]
    private Optional<Expression> parseIdentifierOrFunctionCall() {
        var position = getTokenPosition();
        if (!tokenIsOfType(TokenType.IDENTIFIER)) {
            return Optional.empty();
        }
        var id = (String) token().value();
        consumeToken();
        if (!tokenIsOfType(TokenType.LEFT_PARENTHESES)) {
            return Optional.of(new Identifier(id, position));
        }
        consumeToken();
        var arguments = new ArrayList<Expression>();
        var expression = parseExpression();
        if (expression.isPresent()) {
            arguments.add(expression.get());
        }
        while (tokenIsOfType(TokenType.COMMA)) {
            consumeToken();
            expression = parseExpression();
            if (expression.isEmpty()) {
                throwParserError("Expected expression");
            }
            arguments.add(expression.get());
        }
        if (!tokenIsOfType(TokenType.RIGHT_PARENTHESES)) {
            throwParserError("Expected right parentheses");
        }
        consumeToken();
        return Optional.of(new FunctionCall(id, arguments, position));
    }

    // "(", expression, ")";
    private Optional<Expression> parseNestedExpression() {
        if (!tokenIsOfType(TokenType.LEFT_PARENTHESES)) {
            return Optional.empty();
        }
        consumeToken();
        var expression = parseExpression();
        if (expression.isEmpty()) {
            throwParserError("Expected expression");
        }
        if (!tokenIsOfType(TokenType.RIGHT_PARENTHESES)) {
            throwParserError("Expected right parentheses");
        }
        consumeToken();
        return expression;
    }

    private Optional<Expression> parseStringLiteral() {
        var position = getTokenPosition();
        if (!tokenIsOfType(TokenType.STRING_CONST)) {
            return Optional.empty();
        }
        var value = (String) token().value();
        consumeToken();
        return Optional.of(new StringLiteral(value, position));
    }
}
