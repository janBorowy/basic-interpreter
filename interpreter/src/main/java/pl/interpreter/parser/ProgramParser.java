package pl.interpreter.parser;

import java.util.ArrayList;
import java.util.Optional;
import pl.interpreter.TokenType;

public class ProgramParser extends Parser {

    public ProgramParser(TokenManager tokenManager) {
        super(tokenManager);
    }

    // program ::= { definition };
    public Program parseProgram() {
        var definitions = new ArrayList<Definition>();
        var definition = parseDefinition();
        while (definition.isPresent()) {
            definitions.add(definition.get());
            definition = parseDefinition();
        }
        return new Program(definitions, new Position(1, 1));
    }

    // definition               ::= functionDefinition
    //                            | structureDefinition
    //                            | variantDefinition;
    private Optional<Definition> parseDefinition() {
        return parseStructureDefinition()
                .or(this::parseVariant);
    }

    // structureDefinition ::= "struct " identifier "{" parameters "}";
    private Optional<Definition> parseStructureDefinition() {
        var position = getTokenPosition();
        if (!tokenIsOfType(TokenType.KW_STRUCT)) {
            return Optional.empty();
        }
        consumeToken();
        var id = (String) mustBe(TokenType.IDENTIFIER).value();
        consumeToken();
        mustBe(TokenType.LEFT_CURLY_BRACKET);
        consumeToken();
        var parameters = parseParameters();
        mustBe(TokenType.RIGHT_CURLY_BRACKET);
        consumeToken();
        return Optional.of(new StructureDefinition(id, parameters, position));
    }

    // parameters ::= [ parameterType, identifier { "," parameterType, identifier } ];
    private ParameterSignatureMap parseParameters() {
        var position = getTokenPosition();
        var parameters = new ParameterSignatureMap();
        var parameterTypeEnum = ParameterTypeEnum.parseParameterType(token());
        if (parameterTypeEnum.isEmpty()) {
            return parameters;
        }
        var userType = getUserType(parameterTypeEnum.get());
        consumeToken();
        var id = parseIdentifier();
        parameters.add(id, parameterTypeEnum.get(), userType, position);
        while (tokenIsOfType(TokenType.COMMA)) {
            consumeToken();
            parameterTypeEnum = ParameterTypeEnum.parseParameterType(token());
            if (parameterTypeEnum.isEmpty()) {
                throwParserError("Expected type");
            }
            userType = getUserType(parameterTypeEnum.get());
            consumeToken();
            id = parseIdentifier();
            parameters.add(id, parameterTypeEnum.get(), userType, position);
        }
        return parameters;
    }

    // variantDefinition ::= "variant " identifier "{" identifier { "," identifier } "}";
    private Optional<VariantDefinition> parseVariant() {
        var position = getTokenPosition();
        if (!tokenIsOfType(TokenType.KW_VARIANT)) {
            return Optional.empty();
        }
        consumeToken();
        var structureIds = new ArrayList<String>();
        var id = parseIdentifier();
        mustBe(TokenType.LEFT_CURLY_BRACKET);
        do {
            consumeToken();
            structureIds.add(parseIdentifier());
        } while (tokenIsOfType(TokenType.COMMA));
        mustBe(TokenType.RIGHT_CURLY_BRACKET);
        consumeToken();
        return Optional.of(new VariantDefinition(id, structureIds, position));
    }

    private String getUserType(ParameterTypeEnum parameterTypeEnum) {
        if (parameterTypeEnum == ParameterTypeEnum.USER_TYPE) {
            return (String) token().value();
        }
        return null;
    }

}