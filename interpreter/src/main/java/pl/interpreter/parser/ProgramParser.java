package pl.interpreter.parser;

import java.util.ArrayList;
import java.util.Optional;
import pl.interpreter.TokenType;

public class ProgramParser extends Parser {

    private final SingleStatementParser singleStatementParser;
    private final ExpressionParser expressionParser;

    public ProgramParser(TokenManager tokenManager) {
        super(tokenManager);
        this.expressionParser = new ExpressionParser(tokenManager);
        this.singleStatementParser = new SingleStatementParser(expressionParser, tokenManager);
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
        return parseFunctionDefinition()
                .or(this::parseStructureDefinition)
                .or(this::parseVariant);
    }

    // functionDefinition ::= functionReturnType identifier "(" parameters ")" block;
    private Optional<Definition> parseFunctionDefinition() {
        var position = getTokenPosition();
        var returnType = FunctionReturnTypeEnum.parseFunctionReturnType(token());
        if (returnType.isEmpty()) {
            return Optional.empty();
        }
        var userType = getUserType(returnType.get());
        consumeToken();
        var id = parseMustBeIdentifier();
        mustBe(TokenType.LEFT_PARENTHESES);
        consumeToken();
        var parameters = parseParameters();
        mustBe(TokenType.RIGHT_PARENTHESES);
        consumeToken();
        var block = parseBlock();
        if (block.isEmpty()) {
            throwParserError("Expected block");
        }
        return Optional.of(new FunctionDefinition(new FunctionReturnType(returnType.get(), userType), id, parameters, block.get(), position));
    }

    // block ::= "{" { instruction } "}";
    private Optional<Block> parseBlock() {
        var position = getTokenPosition();
        if (!tokenIsOfType(TokenType.LEFT_CURLY_BRACKET)) {
            return Optional.empty();
        }
        consumeToken();
        var instructions = new ArrayList<Instruction>();
        var instruction = parseInstruction();
        while (instruction.isPresent()) {
            instructions.add(instruction.get());
            instruction = parseInstruction();
        }
        mustBe(TokenType.RIGHT_CURLY_BRACKET);
        consumeToken();
        return Optional.of(new Block(instructions, position));
    }

    private Optional<Instruction> parseInstruction() {
        return singleStatementParser.parseSingleStatement()
                .or(this::parseVar);
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
        var parameterTypeEnum = VariableType.parseVariableType(token());
        if (parameterTypeEnum.isEmpty()) {
            return parameters;
        }
        var userType = getUserType(parameterTypeEnum.get());
        consumeToken();
        var id = parseMustBeIdentifier();
        parameters.add(id, parameterTypeEnum.get(), userType, position);
        while (tokenIsOfType(TokenType.COMMA)) {
            consumeToken();
            parameterTypeEnum = VariableType.parseVariableType(token());
            if (parameterTypeEnum.isEmpty()) {
                throwParserError("Expected type");
            }
            userType = getUserType(parameterTypeEnum.get());
            consumeToken();
            id = parseMustBeIdentifier();
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
        var id = parseMustBeIdentifier();
        mustBe(TokenType.LEFT_CURLY_BRACKET);
        do {
            consumeToken();
            structureIds.add(parseMustBeIdentifier());
        } while (tokenIsOfType(TokenType.COMMA));
        mustBe(TokenType.RIGHT_CURLY_BRACKET);
        consumeToken();
        return Optional.of(new VariantDefinition(id, structureIds, position));
    }

    // var                      ::= "var" initialization ";"
    // initialization           ::= primitiveType identifier "=" expression;
    //                            | identifier identifier "=" expression;
    private Optional<Initialization> parseVar() {
        var position = getTokenPosition();
        if (!tokenIsOfType(TokenType.KW_VAR)) {
            return Optional.empty();
        }
        consumeToken();
        var type = VariableType.parseVariableType(token());
        if (type.isEmpty()) {
            throwParserError("Expected type");
        }
        String userType = null;
        if (type.get() == VariableType.USER_TYPE) {
            userType = (String) token().value();
        }
        consumeToken();
        var id = parseMustBeIdentifier();
        mustBe(TokenType.ASSIGNMENT);
        consumeToken();
        var expression = expressionParser.parseExpression();
        if (expression.isEmpty()) {
            throwParserError("Expected expression");
        }
        mustBe(TokenType.SEMICOLON);
        consumeToken();
        return Optional.of(new Initialization(id, userType, type.get(), true, expression.get(), position));
    }

    private String getUserType(VariableType variableType) {
        if (variableType == VariableType.USER_TYPE) {
            return (String) token().value();
        }
        return null;
    }

    private String getUserType(FunctionReturnTypeEnum functionReturnTypeEnum) {
        if (functionReturnTypeEnum == FunctionReturnTypeEnum.USER_TYPE) {
            return (String) token().value();
        }
        return null;
    }

}