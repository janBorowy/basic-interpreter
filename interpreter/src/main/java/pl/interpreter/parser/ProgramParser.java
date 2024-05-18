package pl.interpreter.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import pl.interpreter.lexical_analyzer.TokenType;

public class ProgramParser extends Parser {

    private final SingleStatementParser singleStatementParser;
    private final ExpressionParser expressionParser;

    public ProgramParser(TokenManager tokenManager) {
        super(tokenManager);
        this.expressionParser = new ExpressionParser(tokenManager);
        this.singleStatementParser = new SingleStatementParser(expressionParser, tokenManager);
    }

    // program ::= { definition };
    public Program parse() {
        Map<String, Definition> definitions = new HashMap<>();
        Optional<Definition> definition = parseDefinition();
        while(definition.isPresent()) {
            var it = definition.get();
            if (definitions.containsKey(it.getId())) {
                throwParserException("Multiple functions with same id found: " + it.getId());
            }
            definitions.put(it.getId(), it);
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
        var returnType = FunctionReturnTypeEnum.parse(token());
        if (returnType.isEmpty()) {
            return Optional.empty();
        }
        var userType = getUserType(returnType.get());
        consumeToken();
        var id = parseMustBeIdentifier();
        mustBe(TokenType.LEFT_PARENTHESES);
        consumeToken();
        var parameters = parseParameterList();
        mustBe(TokenType.RIGHT_PARENTHESES);
        consumeToken();
        var block = parseBlock()
                .orElseThrow(() -> getParserException("Expected block"));
        return Optional.of(new FunctionDefinition(new FunctionReturnType(returnType.get(), userType), id, parameters, block, position));
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


    // instruction ::= block
    //               | singleStatement
    //               | compoundStatement;
    private Optional<Instruction> parseInstruction() {
        return singleStatementParser.parseSingleStatement()
                .or(this::parseBlock)
                .or(this::parseCompoundStatement);
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
        var parameters = parseParameterList();
        mustBe(TokenType.RIGHT_CURLY_BRACKET);
        consumeToken();
        return Optional.of(new StructureDefinition(id, parameters, position));
    }

    // parameters ::= [ parameterType, identifier { "," parameterType, identifier } ];
    private List<Parameter> parseParameterList() {
        var position = getTokenPosition();
        List<Parameter> parameters = new ArrayList<>();
        var parameterTypeEnum = VariableType.parse(token());
        if (parameterTypeEnum.isEmpty()) {
            return parameters;
        }
        var userType = getUserType(parameterTypeEnum.get());
        consumeToken();
        var id = parseMustBeIdentifier();
        parameters.add(new Parameter(new ParameterType(parameterTypeEnum.get(), userType), id, position));
        while (tokenIsOfType(TokenType.COMMA)) {
            consumeToken();
            parameterTypeEnum = VariableType.parse(token());
            if (parameterTypeEnum.isEmpty()) {
                throwParserException("Expected type");
            }
            userType = getUserType(parameterTypeEnum.get());
            consumeToken();
            id = parseMustBeIdentifier();
            parameters.add(new Parameter(new ParameterType(parameterTypeEnum.get(), userType), id, position));
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

    // compoundStatement ::= if
    //                     | while
    //                     | match;
    public Optional<Instruction> parseCompoundStatement() {
        return parseIfStatement()
                .or(this::parseWhileStatement)
                .or(this::parseMatchStatement);
    }

    // if ::= "if" "(" expression ")" instruction [ "else" instruction ];
    public Optional<Instruction> parseIfStatement() {
        var position = getTokenPosition();
        if (!tokenIsOfType(TokenType.KW_IF)) {
            return Optional.empty();
        }
        consumeToken();
        mustBe(TokenType.LEFT_PARENTHESES);
        consumeToken();
        var expression = expressionParser.parseExpression()
                        .orElseThrow(() -> getParserException("Expected expression"));
        mustBe(TokenType.RIGHT_PARENTHESES);
        consumeToken();
        var instruction = parseInstruction()
                .orElseThrow(() -> getParserException("Expected instruction"));
        if (!tokenIsOfType(TokenType.KW_ELSE)) {
            return Optional.of(new IfStatement(expression, instruction, null, position));
        }
        consumeToken();
        var elseInstruction = parseInstruction()
                .orElseThrow(() -> getParserException("Expected instruction"));
        return Optional.of(new IfStatement(expression, instruction, elseInstruction, position));
    }

    // while ::= "while", "(" expression ")", instruction;
    public Optional<WhileStatement> parseWhileStatement() {
        var position = getTokenPosition();
        if (!tokenIsOfType(TokenType.KW_WHILE)) {
            return Optional.empty();
        }
        consumeToken();
        mustBe(TokenType.LEFT_PARENTHESES);
        consumeToken();
        var expression = expressionParser.parseExpression()
                        .orElseThrow(() -> getParserException("Expected expression"));
        mustBe(TokenType.RIGHT_PARENTHESES);
        consumeToken();
        var instruction = parseInstruction()
                .orElseThrow(() -> getParserException("Expected instruction"));
        return Optional.of(new WhileStatement(expression, instruction, position));
    }

    // match ::= "match", "(", dotAccess, ")", "{", matchBranch, {matchBranch}, "}";
    private Optional<Instruction> parseMatchStatement() {
        var position = getTokenPosition();
        if (!tokenIsOfType(TokenType.KW_MATCH)) {
            return Optional.empty();
        }
        consumeToken();
        mustBe(TokenType.LEFT_PARENTHESES);
        consumeToken();
        var expression = expressionParser.parseDotAccess()
                        .orElseThrow(() -> getParserException("Expected variant"));
        mustBe(TokenType.RIGHT_PARENTHESES);
        consumeToken();
        mustBe(TokenType.LEFT_CURLY_BRACKET);
        consumeToken();
        var branches = new ArrayList<MatchBranch>();
        var branch = parseMatchBranch();
        if (branch.isEmpty()) {
            throwParserException("Expected branch");
        }
        branches.add(branch.get());
        branch = parseMatchBranch();
        while (branch.isPresent()) {
            branches.add(branch.get());
            branch = parseMatchBranch();
        }
        mustBe(TokenType.RIGHT_CURLY_BRACKET);
        consumeToken();
        return Optional.of(new MatchStatement(expression, branches, position));
    }

    // matchBranch ::= identifier, identifier, "->" instruction;
    //               | "default" "->" instruction;
    private Optional<MatchBranch> parseMatchBranch() {
        var position = getTokenPosition();
        if (!tokenIsOfType(TokenType.IDENTIFIER)) {
            if (tokenIsOfType(TokenType.KW_DEFAULT)) {
                consumeToken();
                mustBe(TokenType.ARROW);
                consumeToken();
                var instruction = parseInstruction()
                        .orElseThrow(() -> getParserException("Expected instruction"));
                return Optional.of(new MatchBranch(null, null, instruction, position));
            }
            return Optional.empty();
        }
        var structureId = (String) token().value();
        consumeToken();
        var fieldName = parseMustBeIdentifier();
        mustBe(TokenType.ARROW);
        consumeToken();
        var instruction = parseInstruction()
                .orElseThrow(() -> getParserException("Expected instruction"));
        return Optional.of(new MatchBranch(structureId, fieldName, instruction, position));
    }
}