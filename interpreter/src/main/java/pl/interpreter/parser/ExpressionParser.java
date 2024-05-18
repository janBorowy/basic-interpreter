package pl.interpreter.parser;

import java.util.ArrayList;
import java.util.Optional;
import pl.interpreter.lexical_analyzer.TokenType;

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
            var right = parseAlternative()
                    .orElseThrow(() -> getParserException("Expected operand"));
            left = Optional.of(new Conjunction(left.get(), right, position));
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
            var right = parseRelation()
                    .orElseThrow(() -> getParserException("Expected operand"));
            left = Optional.of(new Alternative(left.get(), right, position));
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
        var operator = RelationalOperator.parse(token());
        if (operator.isEmpty()) {
            return left;
        }
        consumeToken();
        var right = parseCast()
                .orElseThrow(() -> getParserException("Expected operand"));
        return Optional.of(new Relation(left.get(), operator.get(), right, position));
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
            var type = PrimitiveType.parse(token())
                    .orElseThrow(() -> getParserException(" Expected primitive type"));
            consumeToken();
            return Optional.of(new Cast(sum.get(), type, position));
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
        var operator = AdditionOperator.parse(token());
        while (operator.isPresent()) {
            consumeToken();
            var right = parseMultiplication()
                    .orElseThrow(() -> getParserException("Expected expression"));
            left = Optional.of(new Sum(left.get(), operator.get(), right, position));
            operator = AdditionOperator.parse(token());
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
        var operator = MultiplicationOperator.parse(token());
        while (operator.isPresent()) {
            consumeToken();
            var right = parseNegation()
                    .orElseThrow(() -> getParserException("Expected expression"));
            left = Optional.of(new Multiplication(left.get(), operator.get(), right, position));
            operator = MultiplicationOperator.parse(token());
        }
        return left;
    }

    // negation ::= ["!"] factor;
    private Optional<Expression> parseNegation() {
        var position = getTokenPosition();
        if (tokenIsOfType(TokenType.NEGATION_OPERATOR)) {
            consumeToken();
            var factor = parseFactor()
                    .orElseThrow(() -> getParserException("Expected expression"));
            return Optional.of(new Negation(factor, position));
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
        }
        if (tokenIsOfType(TokenType.KW_FALSE)) {
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
        parseExpression().ifPresent(arguments::add);
        while (tokenIsOfType(TokenType.COMMA)) {
            consumeToken();
            var expression = parseExpression()
                    .orElseThrow(() -> getParserException("Expected expression"));
            arguments.add(expression);
        }
        mustBe(TokenType.RIGHT_PARENTHESES);
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
            throwParserException("Expected expression");
        }
        mustBe(TokenType.RIGHT_PARENTHESES);
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
