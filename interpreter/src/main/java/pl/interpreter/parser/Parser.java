package pl.interpreter.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import pl.interpreter.Token;
import pl.interpreter.TokenType;
import pl.interpreter.lexical_analyzer.LexicalAnalyzer;
import pl.interpreter.parser.ast.FunctionDefinition;
import pl.interpreter.parser.ast.FunctionParameters;
import pl.interpreter.parser.ast.FunctionSignature;
import pl.interpreter.parser.ast.Node;
import pl.interpreter.parser.ast.ParameterSignature;
import pl.interpreter.parser.ast.Program;
import pl.interpreter.parser.ast.StructureDefinition;
import pl.interpreter.parser.ast.UserType;
import pl.interpreter.parser.ast.VariableType;
import pl.interpreter.parser.ast.VariableTypeEnum;
import pl.interpreter.parser.ast.VariantDefinition;
import pl.interpreter.parser.ast.VoidType;

public class Parser {
    private Token token;
    private final LexicalAnalyzer analyzer;

    public Parser(LexicalAnalyzer analyzer) {
        this.analyzer = analyzer;
        consumeToken();
    }

    // program ::= {functionDefinition | structureDefinition | variantDefinition}
    public Program parseProgram() {
        Node node;
        var functionDefinitions = new ArrayList<FunctionDefinition>();
        var structureDefinitions = new ArrayList<StructureDefinition>();
        var variantDefinitions = new ArrayList<VariantDefinition>();
        if (Objects.nonNull(node = parseFunctionDefinition())) {
            functionDefinitions.add((FunctionDefinition) node);
        }
//        } else if (Objects.isNull(node = parseStructureDefinition())) {
//            structureDefinitions.add((StructureDefinition) node);
//        } else if (Objects.isNull(node = parseVariantDefinition())) {
//            variantDefinitions.add((VariantDefinition) node);
//        }
        return new Program(functionDefinitions, structureDefinitions, variantDefinitions);
    }

    // functionDefinitions ::= functionSignature "(" functionParameters ")" block;
    private Node parseFunctionDefinition() {
        FunctionSignature functionSignature;
        if(Objects.isNull(functionSignature = parseFunctionSignature())) {
            return null;
        }
        mustBe(TokenType.LEFT_PARENTHESES);
        consumeToken();
        var functionParameters = parseFunctionParameters();

        return new FunctionDefinition(functionSignature, functionParameters);
    }

    private FunctionParameters parseFunctionParameters() {
        var parameterSignatures = new ArrayList<ParameterSignature>();
        ParameterSignature node;
        if(Objects.isNull(node = parseParameterSignature())) {
            return null;
        } else {
            parameterSignatures.add(node);
        }
        while(token.type() == TokenType.COMMA) {
            consumeToken();
            parameterSignatures.add(parseParameterSignature());
        }
        return new FunctionParameters(parameterSignatures);
    }

    // parameterSignature ::= variableTypeIdentifier
    private ParameterSignature parseParameterSignature() {
        var type = parseUserOrVariableType();
        if(Objects.isNull(type)) {
            return null;
        }
        mustBe(TokenType.IDENTIFIER);
        var parameterSignature = new ParameterSignature(type, (String) token.value());
        consumeToken();
        return parameterSignature;
    }

    private void mustBe(TokenType tokenType) {
        if(token.type() != tokenType) {
            throwParserException("Invalid token");
        }
    }

    private void mustBeIn(List<TokenType> tokenTypes) {
        if (tokenTypes.stream().noneMatch(t -> token.type() == t)) {
            throwParserException("Invalid token");
        }
    }

    private FunctionSignature parseFunctionSignature() {
        if(token.type() == TokenType.KW_VOID) {
            consumeToken();
            mustBe(TokenType.IDENTIFIER);
            var functionSignature = new FunctionSignature(new VoidType(), (String) token.value());
            consumeToken();
            return functionSignature;
        }
        var returnType = parseUserOrVariableType();
        if(Objects.isNull(returnType)) {
            return null;
        }
        mustBe(TokenType.IDENTIFIER);
        var functionSignature = new FunctionSignature(returnType, (String) token.value());
        consumeToken();
        return functionSignature;
    }

    private Node parseUserOrVariableType() {
        if(TokenTypeGroupsProvider.VAR_TYPES.contains(token.type())) {
            var variableType = new VariableType(VariableTypeEnum.tokenTypeToVariableType(token.type()));
            consumeToken();
            return variableType;
        } else if (token.type() == TokenType.IDENTIFIER) {
            var userType = new UserType((String) token.value());
            consumeToken();
            return userType;
        }
        return null;
    }

//    private Node parseStructureDefinition() {
//        return new StructureDefinition();
//    }
//
//    private Node parseVariantDefinition() {
//        return new VariantDefinition();
//    }

    private void consumeToken() {
        token = analyzer.getNextToken();
    }

    private void throwParserException(String message) {
        throw new ParserException(message, token.row(), token.col());
    }

}
