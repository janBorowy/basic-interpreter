package pl.interpreter.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import pl.interpreter.Token;
import pl.interpreter.TokenType;
import pl.interpreter.lexical_analyzer.LexicalAnalyzer;
import pl.interpreter.parser.ast.AdditiveOperator;
import pl.interpreter.parser.ast.AfterIdentifierStatement;
import pl.interpreter.parser.ast.As;
import pl.interpreter.parser.ast.Block;
import pl.interpreter.parser.ast.CompoundStatement;
import pl.interpreter.parser.ast.Definition;
import pl.interpreter.parser.ast.Expression;
import pl.interpreter.parser.ast.Factor;
import pl.interpreter.parser.ast.FunctionArguments;
import pl.interpreter.parser.ast.FunctionDefinition;
import pl.interpreter.parser.ast.FunctionReturnType;
import pl.interpreter.parser.ast.FunctionReturnTypeEnum;
import pl.interpreter.parser.ast.FunctionSignature;
import pl.interpreter.parser.ast.IdentifierStatement;
import pl.interpreter.parser.ast.IdentifierValueApplier;
import pl.interpreter.parser.ast.If;
import pl.interpreter.parser.ast.Initialization;
import pl.interpreter.parser.ast.InplaceValue;
import pl.interpreter.parser.ast.Instruction;
import pl.interpreter.parser.ast.Match;
import pl.interpreter.parser.ast.MatchBranch;
import pl.interpreter.parser.ast.MultiplicativeOperator;
import pl.interpreter.parser.ast.Node;
import pl.interpreter.parser.ast.Operator;
import pl.interpreter.parser.ast.ParameterSignature;
import pl.interpreter.parser.ast.PrimitiveInitialization;
import pl.interpreter.parser.ast.Program;
import pl.interpreter.parser.ast.Return;
import pl.interpreter.parser.ast.SingleStatement;
import pl.interpreter.parser.ast.StructureDefinition;
import pl.interpreter.parser.ast.Term;
import pl.interpreter.parser.ast.UserTypeInitialization;
import pl.interpreter.parser.ast.Value;
import pl.interpreter.parser.ast.ValueAssignment;
import pl.interpreter.parser.ast.VarInitialization;
import pl.interpreter.parser.ast.VariableAssignment;
import pl.interpreter.parser.ast.VariableType;
import pl.interpreter.parser.ast.VariantDefinition;
import pl.interpreter.parser.ast.While;

// TODO: fix initialization

public class Parser {
    private Token token;
    private final LexicalAnalyzer analyzer;

    public Parser(LexicalAnalyzer analyzer) {
        this.analyzer = analyzer;
        consumeToken();
    }

    // program ::= { definition };
    public Program parseProgram() {
        var definitions = new ArrayList<Definition>();
        var definition = parseDefinition();
        while (definition.isPresent()) {
            definitions.add(definition.get());
            definition = parseDefinition();
        }
        if (token.type() != TokenType.EOF) {
            throwParserException("Failed to parse source");
        }
        return new Program(definitions);
    }

    // definition               ::= functionDefinition
    //                           | structureDefinition
    //                           | variantDefinition;
    private Optional<Definition> parseDefinition() {
        var functionDefinition = parseFunctionDefinition();
        if (functionDefinition.isPresent()) {
            return Optional.of(functionDefinition.get());
        }
        var structureDefinition = parseStructureDefinition();
        if (structureDefinition.isPresent()) {
            return Optional.of(structureDefinition.get());
        }
        var variantDefinition = parseVariantDefinition();
        if (variantDefinition.isPresent()) {
            return Optional.of(variantDefinition.get());
        }
        return Optional.empty();
    }

    // functionDefinitions ::= functionSignature "(" [ parameterSignature { "," parameterSignature } ] ")" block;
    private Optional<FunctionDefinition> parseFunctionDefinition() {
        var functionSignature = parseFunctionSignature();
        if (functionSignature.isEmpty()) {
            return Optional.empty();
        }
        mustBe(TokenType.LEFT_PARENTHESES);
        consumeToken();
        var functionParameters = new ArrayList<ParameterSignature>();
        var parameterSignature = parseParameterSignature();
        parameterSignature.ifPresent(functionParameters::add);
        while (token.type() == TokenType.COMMA) {
            consumeToken();
            var node = parseParameterSignature();
            if (node.isEmpty()) {
                throwParserException("Expected parameter");
            }
            functionParameters.add(node.get());
        }
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
        if (token.type() != TokenType.KW_STRUCT) {
            return Optional.empty();
        }
        consumeToken();
        mustBe(TokenType.IDENTIFIER);
        var identifier = (String) token.value();
        consumeToken();
        mustBe(TokenType.LEFT_CURLY_BRACKET);
        consumeToken();
        var signatures = new ArrayList<ParameterSignature>();
        var signature = parseParameterSignature();
        while (signature.isPresent()) {
            signatures.add(signature.get());
            mustBe(TokenType.SEMICOLON);
            consumeToken();
            signature = parseParameterSignature();
        }
        mustBe(TokenType.RIGHT_CURLY_BRACKET);
        consumeToken();
        return Optional.of(new StructureDefinition(identifier, signatures));
    }
    // TODO
    // variantDefinition ::= "variant " identifier "{" identifier { "," identifier }; "}";
    private Optional<VariantDefinition> parseVariantDefinition() {
        if (token.type() != TokenType.KW_VARIANT) {
            return Optional.empty();
        }
        consumeToken();
        if (token.type() != TokenType.IDENTIFIER) {
            throwParserException("Expected identifier");
        }
        var variantName = (String) token.value();
        consumeToken();
        mustBe(TokenType.LEFT_CURLY_BRACKET);
        consumeToken();
        var identifiers = new ArrayList<String>();
        mustBe(TokenType.IDENTIFIER);
        var identifier = (String) token.value();
        identifiers.add(identifier);
        consumeToken();
        while (token.type() == TokenType.COMMA) {
            consumeToken();
            if (token.type() != TokenType.IDENTIFIER) {
                throwParserException("Expected identifier");
            }
            identifiers.add((String) token.value());
            consumeToken();
        }
        mustBe(TokenType.RIGHT_CURLY_BRACKET);
        consumeToken();
        return Optional.of(new VariantDefinition(variantName, identifiers));
    }

    // block ::= "{" { singleOrCompoundStatement } "}";
    private Optional<Block> parseBlock() {
        if(token.type() != TokenType.LEFT_CURLY_BRACKET) {
            return Optional.empty();
        }
        consumeToken();
        var statements = new ArrayList<Instruction>();
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
    private Optional<Instruction> parseSingleOrCompoundStatement() {
        var singleStatement = parseSingleStatement();
        if (singleStatement.isPresent()) {
            mustBe(TokenType.SEMICOLON);
            consumeToken();
            return Optional.of(singleStatement.get());
        }
        return parseCompoundStatement().map(s -> s);
    }

    // TODO: refactor single statement
    // singleStatement ::= identifierStatement | varInitialization | primitiveInitalization | return;
    private Optional<SingleStatement> parseSingleStatement() {
        var initialization = parseInitialization();
        if (initialization.isPresent()) {
            return Optional.of(initialization.get());
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
    // varInitialization ::= "var", initialization;
    private Optional<VarInitialization> parseVarInitialization() {
        return Optional.empty();
    }

    // TODO
    // initialization ::= primitiveInitialization
    //                  | userTypeInitialization;
    private Optional<Initialization> parseInitialization () {
        return Optional.empty();
    }

    // TODO
    // userTypeInitialization ::= identifier variableAssignment;
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
    // TODO
    // while ::= "while", "(" condition ")", instruction;
    private Optional<While> parseWhile() {
        return Optional.empty();
    }
    // TODO
    // instruction              ::= block
    //                           | singleStatement
    //                           | compoundStatement;
    private Optional<Instruction> parseInstruction() {
        return Optional.empty();
    }

    private Optional<Match> parseMatch() {
        return Optional.empty();
    }

    private Optional<MatchBranch> parseMatchBranch() {
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

    // additiveOperator         ::= "+"
    //                            | "-";
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

    // multiplicativeOperator   ::= "*"
    //                           | "/"
    //                           | "%";
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

    // TODO
    // factor                   ::= number
    //                           | identifier, [identifierValueApplier]
    //                           | "(", expression, ")";
    private Optional<Factor> parseFactor() {
        return Optional.empty();
    }

    // TODO
    // identifierValueApplier   ::= functionArguments
    //                            | as;
    private Optional<IdentifierValueApplier> parseIdentifierValueApplier() {
        return Optional.empty();
    }

    // TODO
    // if ::= "if" "(" condition ")" instruction [ "else" instruction ];
    private Optional<If> parseIf() {
        return Optional.empty();
    }

    // condition                ::= subcondition, {" and ", subcondition};
    // subcondition             ::= negatableBooleanExpression, {" or ", negatableBooleanExpression};
    // negatableBooleanExpression ::= ["!"], booleanExpression;
    // booleanExpression        ::= value [arithmeticCondition]
    //                           | "(" condition ")";
    // arithmeticCondition      ::= equal
    //                           | notEqual
    //                           | lessThan
    //                           | greaterThan
    //                           | lessThanOrEqual
    //                           | greaterThanOrEqual;
    // equal                    ::= "==", value;
    // notEqual                 ::= "!=", value;
    // lessThan                 ::= "<", value;
    // greaterThan              ::= ">", value;
    // lessThanOrEqual          ::= "<=", value;
    // greaterThanOrEqual       ::= ">=", value;

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

    // TODO
    // value ::= expression
    //         | inplaceValue;
    private Optional<Value> parseValue() {
        return Optional.empty();
    }

    // TODO
    // inplaceValue             ::= number
    //                           | stringLiteral
    //                           | booleanLiteral;
    private Optional<InplaceValue> parseInplaceValue() {
//        if (token.type() == TokenType.STRING_CONST) {
//            var value = token.value();
//            consumeToken();
//            return Optional.of(new StringConst((String) value));
//        }
//        if (token.type() == TokenType.INT_CONST) {
//            var value = token.value();
//            consumeToken();
//            return Optional.of(new IntConst((int) value));
//        }
//        if (token.type() == TokenType.FLOAT_CONST) {
//            var value = token.value();
//            consumeToken();
//            return Optional.of(new FloatConst((float) value));
//        }
//        if (TokenTypeGroupsProvider.BOOL_TYPES.contains(token.type())) {
//            var value = token.value();
//            consumeToken();
//            return Optional.of(new BoolConst(value.equals("true")));
//        }
        return Optional.empty();
    }

    // TODO
    // as ::= " as ", variableType;
    private Optional<As> parseAs() {
        return Optional.empty();
    }

    // TODO
    // functionSignature ::= functionReturnType identifier;
    private Optional<FunctionSignature> parseFunctionSignature() {
        var functionReturnType = parseFunctionReturnType();
        if (functionReturnType.isEmpty()) {
            return Optional.empty();
        }
        mustBe(TokenType.IDENTIFIER);
        var identifier = token.value();
        consumeToken();
        return Optional.of(new FunctionSignature(functionReturnType.get(), (String) identifier));
    }

    // functionReturnType       ::= variableType
    //                           | "void"
    //                           | identifier;
    private Optional<FunctionReturnType> parseFunctionReturnType() {
        if (TokenTypeGroupsProvider.VAR_TYPES.contains(token.type())) {
            var type = token.type();
            consumeToken();
            return Optional.of(new FunctionReturnType(FunctionReturnTypeEnum.getFromTokenType(type)));
        }
        if (token.type() == TokenType.KW_VOID) {
            consumeToken();
            return Optional.of(new FunctionReturnType(FunctionReturnTypeEnum.VOID));
        }
        if (token.type() == TokenType.IDENTIFIER) {
            var identifier = (String) token.value();
            consumeToken();
            return Optional.of(new FunctionReturnType(FunctionReturnTypeEnum.USER_TYPE, Optional.of(identifier)));
        }
        return Optional.empty();
    }

    // parameterSignature ::= variableType, identifier
    //                      | identifier, identifier;
    private Optional<ParameterSignature> parseParameterSignature() {
        var variableType = getVariableType();
        if (variableType.isPresent()) {
            mustBe(TokenType.IDENTIFIER);
            var identifier = (String) token.value();
            consumeToken();
            return Optional.of(new ParameterSignature(variableType.get(), identifier));
        }
        if (token.type() != TokenType.IDENTIFIER) {
            return Optional.empty();
        }
        var userType = (String) token.value();
        consumeToken();
        if (token.type() != TokenType.IDENTIFIER) {
            throwParserException("Expected identifier");
        }
        var identifier = (String) token.value();
        consumeToken();
        return Optional.of(new ParameterSignature(VariableType.USER_TYPE, identifier, Optional.of(userType)));
    }

    // variableType             ::= "int"
    //                           | "float"
    //                           | "string"
    //                           | "bool";
    private Optional<VariableType> getVariableType() {
        if (TokenTypeGroupsProvider.VAR_TYPES.contains(token.type())) {
            var type = token.type();
            consumeToken();
            return Optional.of(VariableType.getFromTokenType(type));
        }
        return Optional.empty();
    }

    private void mustBe(TokenType tokenType) {
        if (token.type() != tokenType) {
            throwParserException("Invalid token");
        }
    }

    private void consumeToken() {
        token = analyzer.getNextToken();
    }

    private void throwParserException(String message) {
        throw new ParserException(message, token.row(), token.col());
    }

}
