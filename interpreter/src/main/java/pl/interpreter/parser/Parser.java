package pl.interpreter.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import pl.interpreter.Token;
import pl.interpreter.TokenType;
import pl.interpreter.lexical_analyzer.LexicalAnalyzer;
import pl.interpreter.parser.ast.AdditiveOperator;
import pl.interpreter.parser.ast.AfterIdentifierStatement;
import pl.interpreter.parser.ast.Assignment;
import pl.interpreter.parser.ast.Block;
import pl.interpreter.parser.ast.BoolConst;
import pl.interpreter.parser.ast.CompoundStatement;
import pl.interpreter.parser.ast.Expression;
import pl.interpreter.parser.ast.Factor;
import pl.interpreter.parser.ast.FloatConst;
import pl.interpreter.parser.ast.FunctionArguments;
import pl.interpreter.parser.ast.FunctionDefinition;
import pl.interpreter.parser.ast.FunctionParameters;
import pl.interpreter.parser.ast.FunctionSignature;
import pl.interpreter.parser.ast.IdentifierStatement;
import pl.interpreter.parser.ast.Initialization;
import pl.interpreter.parser.ast.InitializationSignature;
import pl.interpreter.parser.ast.IntConst;
import pl.interpreter.parser.ast.MultiplicativeOperator;
import pl.interpreter.parser.ast.Node;
import pl.interpreter.parser.ast.Operator;
import pl.interpreter.parser.ast.ParameterSignature;
import pl.interpreter.parser.ast.PrimitiveInitialization;
import pl.interpreter.parser.ast.Program;
import pl.interpreter.parser.ast.Return;
import pl.interpreter.parser.ast.StringConst;
import pl.interpreter.parser.ast.StructureDefinition;
import pl.interpreter.parser.ast.Term;
import pl.interpreter.parser.ast.UserType;
import pl.interpreter.parser.ast.UserTypeInitialization;
import pl.interpreter.parser.ast.Value;
import pl.interpreter.parser.ast.ValueAssignment;
import pl.interpreter.parser.ast.VarInitialization;
import pl.interpreter.parser.ast.VariableAssignment;
import pl.interpreter.parser.ast.VariableType;
import pl.interpreter.parser.ast.VariableTypeEnum;
import pl.interpreter.parser.ast.VariantDefinition;
import pl.interpreter.parser.ast.VoidType;

// TODO: fix initialization

public class Parser {
    private Token token;
    private final LexicalAnalyzer analyzer;

    public Parser(LexicalAnalyzer analyzer) {
        this.analyzer = analyzer;
        consumeToken();
    }

    // program ::= {functionDefinition | structureDefinition | variantDefinition}
    public Program parseProgram() {
        var functionDefinitions = new ArrayList<FunctionDefinition>();
        var structureDefinitions = new ArrayList<StructureDefinition>();
        var variantDefinitions = new ArrayList<VariantDefinition>();
        var node = parseFunctionDefinition();
        while (node.isPresent()) {
            functionDefinitions.add(node.get());
            node = parseFunctionDefinition();
        }
//        } else if (Objects.isNull(node = parseStructureDefinition())) {
//            structureDefinitions.add((StructureDefinition) node);
//        } else if (Objects.isNull(node = parseVariantDefinition())) {
//            variantDefinitions.add((VariantDefinition) node);
//        }
        return new Program(functionDefinitions, structureDefinitions, variantDefinitions);
    }

    // functionDefinitions ::= functionSignature "(" functionParameters ")" block;
    private Optional<FunctionDefinition> parseFunctionDefinition() {
        var functionSignature = parseFunctionSignature();
        if (functionSignature.isEmpty()) {
            return Optional.empty();
        }
        mustBe(TokenType.LEFT_PARENTHESES);
        consumeToken();
        var functionParameters = parseFunctionParameters();
        mustBe(TokenType.RIGHT_PARENTHESES);
        consumeToken();
        var block = parseBlock();
        if (block.isEmpty()) {
            throwParserException("Block expected");
        }
        return Optional.of(new FunctionDefinition(functionSignature.get(), functionParameters, block.get()));
    }
    // TODO
    // structureDefinition ::= "struct " identifier "{" { parameterSignature ";" } "}";
    private Optional<StructureDefinition> parseStructureDefinition() {
        return Optional.empty();
    }
    // TODO
    // variantDefinition ::= "variant " identifier "{" identifierList "}";
    private Optional<VariantDefinition> parseVariantDefinition() {
        return Optional.empty();
    }

    // functionParameters ::= [parameterSignature { "," parameterSignature } ];
    private FunctionParameters parseFunctionParameters() {
        var parameterSignatures = new ArrayList<ParameterSignature>();
        var parameterSignature = parseParameterSignature();
        if (parameterSignature.isEmpty()) {
            return new FunctionParameters(List.of());
        } else {
            parameterSignatures.add(parameterSignature.get());
        }
        while (token.type() == TokenType.COMMA) {
            consumeToken();
            var node = parseParameterSignature();
            if (node.isEmpty()) {
                throwParserException("Expected parameter");
            }
            parameterSignatures.add(node.get());
        }
        return new FunctionParameters(parameterSignatures);
    }

    // block ::= "{" { singleOrCompoundStatement } "}";
    private Optional<Block> parseBlock() {
        if(token.type() != TokenType.LEFT_CURLY_BRACKET) {
            return Optional.empty();
        }
        consumeToken();
        var statements = new ArrayList<Node>();
        var statement = parseSingleOrCompoundStatement();
        while (statement.isPresent()) {
            statements.add(statement.get());
            statement = parseSingleOrCompoundStatement();
        }
        mustBe(TokenType.RIGHT_CURLY_BRACKET);
        consumeToken();
        return Optional.of(new Block(statements));
    }

    // singleOrCompoundStatement ::= singleStatement, ";" | compoundStatement
    private Optional<Node> parseSingleOrCompoundStatement() {
        var singleStatement = parseSingleStatement();
        if (singleStatement.isPresent()) {
            mustBe(TokenType.SEMICOLON);
            consumeToken();
            return Optional.of(singleStatement.get());
        }
        var compoundStatement = parseCompoundStatement();
        return compoundStatement.map(CompoundStatement::new);
    }

    // TODO: refactor single statement
    // singleStatement ::= identifierStatement | varInitialization | primitiveInitalization | return;
    private Optional<Node> parseSingleStatement() {
        var initialization = parseInitialization();
        if (initialization.isPresent()) {
            return initialization;
        }
        var returnStatement = parseReturn();
        if (returnStatement.isPresent()) {
            return Optional.of(returnStatement.get());
        }
        var identifierStatement = parseIdentifierStatement();
        if (identifierStatement.isPresent()) {
            return Optional.of(identifierStatement.get());
        }
        return Optional.empty();
    }

    // TODO
    // compoundStatement ::= if | while | match;
    private Optional<CompoundStatement> parseCompoundStatement() {
        return Optional.empty();
    }

    // TODO
    // identifierStatement ::= identifier, valueAssignment | functionAssignment | variableAssignment;
    private Optional<IdentifierStatement> parseIdentifierStatement() {
        return Optional.empty();
    }

    // TODO
    // afterIdentifierStatement ::= valueAssignment
    //                            | functionArguments
    //                            | variableAssignment;
    private Optional<AfterIdentifierStatement> parseAfterIdentifierStatement() {
        return Optional.empty();
    }

    // TODO
    // varInitialization ::= "var", primitiveInitialization | userTypeInitialization;
    private Optional<VarInitialization> parseVarInitialization() {
        return Optional.empty();
    }

    // TODO
    // initialization ::= primitiveInitialization
    //                  | userTypeInitialization;
    private Optional<Initialization> parseInitialization () {
        return Optional.empty();
    }

    private Optional<UserTypeInitialization> parseUserTypeInitialization() {
        return Optional.empty();
    }

    // TODO
    // primitiveInitialization ::= variableType variableAssignment;
    private Optional<PrimitiveInitialization> parsePrimitiveInitialization() {
        return Optional.empty();
    }

    // TODO
    // variableAssignment ::= identifier valueAssignment;
    private Optional<VariableAssignment> parseVariableAssignment() {
        return Optional.empty();
    }

    // TODO
    // valueAssignment ::= "=", value;
    private Optional<ValueAssignment> parseValueAssignment() {
        return Optional.empty();
    }

    // return ::= "return", [value];
    private Optional<Return> parseReturn() {
        if (token.type() == TokenType.KW_RETURN) {
            consumeToken();
            var value = parseValue();
            return Optional.of(new Return(value));
        }
        return Optional.empty();
    }

    // functionArguments ::= "(", [ value { "," value } ], ")";
    private Optional<FunctionArguments> parseFunctionArguments() {
        if (token.type() != TokenType.LEFT_PARENTHESES) {
            return Optional.empty();
        }
        consumeToken();
        var values = new ArrayList<Value>();
        var node = parseValue();
        if (node.isPresent()) {
            values.add(node.get());
            while (token.type() == TokenType.COMMA) {
                consumeToken();
                node = parseValue();
                if (node.isEmpty()) {
                    throwParserException("Expected value");
                }
                values.add(node.get());
            }
        }
        return Optional.of(new FunctionArguments(values));
    }

    // TODO: finish parseValue (as is missing)
    // value ::= expression | functionCall
    private Optional<Value> parseValue() {
        var expression = parseExpression();
        if (expression.isPresent()) {
            return Optional.of(new Value(expression.get()));
        }
//        var functionCall = parseFunctionCall();
//        if (functionCall.isPresent()) {
//            return Optional.of(new Value(functionCall.get()));
//        }
//        var as = parseAs();
//        if (as.isPresent()) {
//            return Optional.of(new Value(as.get()));
//        }
        return Optional.empty();
    }

    // expression ::= term, { additiveOperator, term };
    private Optional<Expression> parseExpression() {
        var terms = new ArrayList<Term>();
        var additiveOperators = new ArrayList<AdditiveOperator>();
        var term = parseTerm();
        if (term.isEmpty()) {
            return Optional.empty();
        }
        terms.add(term.get());
        var operator = parseAdditiveOperator();
        while (operator.isPresent()) {
            var node = parseTerm();
            if (node.isEmpty()) {
                throwParserException("Expected term");
            }
            terms.add(node.get());
            additiveOperators.add(operator.get());
            operator = parseAdditiveOperator();
        }
        return Optional.of(new Expression(terms, additiveOperators));
    }

    // term ::= factor, { multiplicativeOperator, factor };
    private Optional<Term> parseTerm() {
        var factors = new ArrayList<Factor>();
        var multiplicativeOperators = new ArrayList<MultiplicativeOperator>();
        var factor = parseFactor();
        if (factor.isEmpty()) {
            return Optional.empty();
        }
        factors.add(factor.get());
        var operator = parseMultiplicativeOperator();
        while (operator.isPresent()) {
            var node = parseFactor();
            if (node.isEmpty()) {
                throwParserException("Expected factor");
            }
            factors.add(node.get());
            multiplicativeOperators.add(operator.get());
        }
        return Optional.of(new Term(factors, multiplicativeOperators));
    }

    private Optional<AdditiveOperator> parseAdditiveOperator() {
        if (token.type() == TokenType.ADD_OPERATOR) {
            consumeToken();
            return Optional.of(new AdditiveOperator(Operator.ADD));
        }
        if (token.type() == TokenType.SUBTRACT_OPERATOR) {
            consumeToken();
            return Optional.of(new AdditiveOperator(Operator.SUBTRACT));
        }
        return Optional.empty();
    }

    private Optional<Factor> parseFactor() {
        if (token.type() == TokenType.IDENTIFIER) {
            consumeToken();
            return Optional.of(new Factor(token.value()));
        }
        var constant = parseConstant();
        if (constant.isPresent()) {
            return Optional.of(new Factor(constant.get()));
        }
        if (token.type() == TokenType.LEFT_PARENTHESES) {
            consumeToken();
            var expression = parseExpression();
            if (expression.isEmpty()) {
                throwParserException("Expression expected");
            }
            mustBe(TokenType.RIGHT_PARENTHESES);
            consumeToken();
            return Optional.of(new Factor(expression.get()));
        }
        return Optional.empty();
    }

    private Optional<MultiplicativeOperator> parseMultiplicativeOperator() {
        if (token.type() == TokenType.MULTIPLY_OPERATOR) {
            consumeToken();
            return Optional.of(new MultiplicativeOperator(Operator.MULTIPLY));
        }
        if (token.type() == TokenType.DIVIDE_OPERATOR) {
            consumeToken();
            return Optional.of(new MultiplicativeOperator(Operator.DIVIDE));
        }
        if (token.type() == TokenType.MODULO_OPERATOR) {
            consumeToken();
            return Optional.of(new MultiplicativeOperator(Operator.MODULO));
        }
        return Optional.empty();
    }

    // constant ::= string_const | int_const | float_const | bool_const
    private Optional<Node> parseConstant() {
        if (token.type() == TokenType.STRING_CONST) {
            var value = token.value();
            consumeToken();
            return Optional.of(new StringConst((String) value));
        }
        if (token.type() == TokenType.INT_CONST) {
            var value = token.value();
            consumeToken();
            return Optional.of(new IntConst((int) value));
        }
        if (token.type() == TokenType.FLOAT_CONST) {
            var value = token.value();
            consumeToken();
            return Optional.of(new FloatConst((float) value));
        }
        if (TokenTypeGroupsProvider.BOOL_TYPES.contains(token.type())) {
            var value = token.value();
            consumeToken();
            return Optional.of(new BoolConst(value.equals("true")));
        }
        return Optional.empty();
    }

    // parameterSignature ::= variableTypeIdentifier
    private Optional<ParameterSignature> parseParameterSignature() {
        var type = parseUserOrVariableType();
        if (type.isEmpty()) {
            return Optional.empty();
        }
        mustBe(TokenType.IDENTIFIER);
        var parameterSignature = new ParameterSignature(type.get(), (String) token.value());
        consumeToken();
        return Optional.of(parameterSignature);
    }

    private void mustBe(TokenType tokenType) {
        if (token.type() != tokenType) {
            throwParserException("Invalid token");
        }
    }

    private void mustBeIn(List<TokenType> tokenTypes) {
        if (tokenTypes.stream().noneMatch(t -> token.type() == t)) {
            throwParserException("Invalid token");
        }
    }

    // functionSignature ::= "void ", identifier | variableTypeIdentifier
    private Optional<FunctionSignature> parseFunctionSignature() {
        if (token.type() == TokenType.KW_VOID) {
            consumeToken();
            mustBe(TokenType.IDENTIFIER);
            var functionSignature = new FunctionSignature(new VoidType(), (String) token.value());
            consumeToken();
            return Optional.of(functionSignature);
        }
        var returnType = parseUserOrVariableType();
        if (returnType.isEmpty()) {
            return Optional.empty();
        }
        mustBe(TokenType.IDENTIFIER);
        var functionSignature = new FunctionSignature(returnType.get(), (String) token.value());
        consumeToken();
        return Optional.of(functionSignature);
    }

    // variableTypeIdentifier ::= variableType, " ", identifier
    private Optional<Node> parseUserOrVariableType() {
        if (TokenTypeGroupsProvider.VAR_TYPES.contains(token.type())) {
            var variableType = new VariableType(VariableTypeEnum.tokenTypeToVariableType(token.type()));
            consumeToken();
            return Optional.of(variableType);
        } else if (token.type() == TokenType.IDENTIFIER) {
            var userType = new UserType((String) token.value());
            consumeToken();
            return Optional.of(userType);
        }
        return Optional.empty();
    }

    private void consumeToken() {
        token = analyzer.getNextToken();
    }

    private void throwParserException(String message) {
        throw new ParserException(message, token.row(), token.col());
    }

}
