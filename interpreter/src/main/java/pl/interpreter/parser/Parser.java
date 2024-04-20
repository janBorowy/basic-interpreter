package pl.interpreter.parser;

import java.util.ArrayList;
import java.util.Optional;
import pl.interpreter.Token;
import pl.interpreter.TokenType;
import pl.interpreter.lexical_analyzer.LexicalAnalyzer;
import pl.interpreter.parser.ast.AdditiveOperator;
import pl.interpreter.parser.ast.ArithmeticCondition;
import pl.interpreter.parser.ast.As;
import pl.interpreter.parser.ast.Block;
import pl.interpreter.parser.ast.BooleanExpression;
import pl.interpreter.parser.ast.BooleanLiteral;
import pl.interpreter.parser.ast.CompoundStatement;
import pl.interpreter.parser.ast.LogicTerm;
import pl.interpreter.parser.ast.Parentheses;
import pl.interpreter.parser.ast.Definition;
import pl.interpreter.parser.ast.Expression;
import pl.interpreter.parser.ast.Factor;
import pl.interpreter.parser.ast.FloatConst;
import pl.interpreter.parser.ast.FunctionCall;
import pl.interpreter.parser.ast.FunctionDefinition;
import pl.interpreter.parser.ast.FunctionReturnType;
import pl.interpreter.parser.ast.FunctionReturnTypeEnum;
import pl.interpreter.parser.ast.FunctionSignature;
import pl.interpreter.parser.ast.IdentifierStatement;
import pl.interpreter.parser.ast.IdentifierValueApplier;
import pl.interpreter.parser.ast.IdentifierWithValue;
import pl.interpreter.parser.ast.If;
import pl.interpreter.parser.ast.Initialization;
import pl.interpreter.parser.ast.InplaceValue;
import pl.interpreter.parser.ast.Instruction;
import pl.interpreter.parser.ast.IntConst;
import pl.interpreter.parser.ast.Match;
import pl.interpreter.parser.ast.MatchBranch;
import pl.interpreter.parser.ast.MultiplicativeOperator;
import pl.interpreter.parser.ast.Node;
import pl.interpreter.parser.ast.Number;
import pl.interpreter.parser.ast.Operator;
import pl.interpreter.parser.ast.ParameterSignature;
import pl.interpreter.parser.ast.PrimitiveInitialization;
import pl.interpreter.parser.ast.Program;
import pl.interpreter.parser.ast.Relation;
import pl.interpreter.parser.ast.Return;
import pl.interpreter.parser.ast.SingleStatement;
import pl.interpreter.parser.ast.StringLiteral;
import pl.interpreter.parser.ast.StructureDefinition;
import pl.interpreter.parser.ast.Subcondition;
import pl.interpreter.parser.ast.Term;
import pl.interpreter.parser.ast.UserTypeInitialization;
import pl.interpreter.parser.ast.Value;
import pl.interpreter.parser.ast.ValueAssignment;
import pl.interpreter.parser.ast.VarInitialization;
import pl.interpreter.parser.ast.VariableAssignment;
import pl.interpreter.parser.ast.VariableType;
import pl.interpreter.parser.ast.VariantDefinition;
import pl.interpreter.parser.ast.While;

// TODO: split parser
// TODO: pass position information to generated tree
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

    // definition              ::= functionDefinition
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

    // singleOrCompoundStatement ::= singleStatement | compoundStatement
    private Optional<Instruction> parseSingleOrCompoundStatement() {
        return parseSingleStatement().map(Instruction.class::cast)
                .or(() -> parseCompoundStatement().map(Instruction.class::cast));
    }

    // singleStatement ::= identifierStatement | varInitialization | primitiveInitalization | return, ";";
    private Optional<SingleStatement> parseSingleStatement() {
        var statement = parseIdentifierStatement().map(SingleStatement.class::cast)
                .or(() -> parseVarInitialization().map(SingleStatement.class::cast))
                .or(() -> parsePrimitiveInitialization().map(SingleStatement.class::cast))
                .or(() -> parseReturn().map(SingleStatement.class::cast));
        if (statement.isEmpty()) {
            return Optional.empty();
        }
        mustBe(TokenType.SEMICOLON);
        consumeToken();
        return statement;
    }

    // compoundStatement ::= if | while | match;
    private Optional<CompoundStatement> parseCompoundStatement() {
        return parseIf().map(CompoundStatement.class::cast)
                .or(() -> parseWhile().map(CompoundStatement.class::cast))
                .or(() -> parseMatch().map(CompoundStatement.class::cast));
    }

    // identifierStatement ::= identifier, valueAssignment | functionArguments | variableAssignment;
    private Optional<IdentifierStatement> parseIdentifierStatement() {
        if (token.type() != TokenType.IDENTIFIER) {
            return Optional.empty();
        }
        var identifier = (String) token.value();
        consumeToken();
        var statement = parseValueAssignment().map(Node.class::cast)
            .or(() -> parseFunctionCall().map(Node.class::cast))
            .or(() -> parseVariableAssignment().map(Node.class::cast));
        if (statement.isEmpty()) {
            throwParserException("Expected statement");
        }
        return Optional.of(new IdentifierStatement(identifier, statement.get()));
    }

    // varInitialization ::= "var", initialization;
    private Optional<VarInitialization> parseVarInitialization() {
        if (token.type() != TokenType.KW_VAR) {
           return Optional.empty();
        }
        consumeToken();
        var initialization = parseInitialization();
        if (initialization.isEmpty()) {
            throwParserException("Expected initialization");
        }
        return Optional.of(new VarInitialization(initialization.get()));
    }

    // initialization ::= primitiveInitialization
    //                  | userTypeInitialization;
    private Optional<Initialization> parseInitialization () {
        return parsePrimitiveInitialization().map(Initialization.class::cast)
                .or(() -> parseUserTypeInitialization().map(Initialization.class::cast));
    }

    // userTypeInitialization ::= identifier variableAssignment;
    private Optional<UserTypeInitialization> parseUserTypeInitialization() {
        if (token.type() != TokenType.IDENTIFIER) {
            return Optional.empty();
        }
        var typeIdentifier = (String) token.value();
        consumeToken();
        var variableAssignment = parseVariableAssignment();
        if (variableAssignment.isEmpty()) {
            throwParserException("Expected variable assignment");
        }
        var assignment = variableAssignment.get();
        return Optional.of(new UserTypeInitialization(typeIdentifier, assignment.identifier(), assignment.valueAssigned()));
    }

    // primitiveInitialization ::= variableType variableAssignment;
    private Optional<PrimitiveInitialization> parsePrimitiveInitialization() {
        var variableType = parseVariableType();
        if (variableType.isEmpty()) {
            return Optional.empty();
        }
        var variableAssignment = parseVariableAssignment();
        if (variableAssignment.isEmpty()) {
            throwParserException("Expected variable assignment");
        }
        var assignment = variableAssignment.get();
        return Optional.of(new PrimitiveInitialization(variableType.get(), assignment.identifier(), assignment.valueAssigned()));
    }

    // variableAssignment ::= identifier valueAssignment;
    private Optional<VariableAssignment> parseVariableAssignment() {
        if (token.type() != TokenType.IDENTIFIER) {
            return Optional.empty();
        }
        var identifier = (String) token.value();
        consumeToken();
        var valueAssignment = parseValueAssignment();
        if (valueAssignment.isEmpty()) {
            throwParserException("Expected value assignment");
        }
        return Optional.of(new VariableAssignment(identifier, valueAssignment.get().value()));
    }

    // valueAssignment ::= "=", value;
    private Optional<ValueAssignment> parseValueAssignment() {
        if (token.type() != TokenType.ASSIGNMENT) {
            return Optional.empty();
        }
        consumeToken();
        var value = parseValue();
        if (value.isEmpty()) {
            throwParserException("Expected value");
        }
        return Optional.of(new ValueAssignment(value.get()));
    }

    // return ::= "return", [value];
    private Optional<Return> parseReturn() {
        if (token.type() != TokenType.KW_RETURN) {
            return Optional.empty();
        }
        consumeToken();
        return Optional.of(new Return(parseValue()));
    }

    // while ::= "while", "(" parentheses ")", instruction;
    private Optional<While> parseWhile() {
        if (token.type() != TokenType.KW_WHILE) {
            return Optional.empty();
        }
        consumeToken();
        mustBe(TokenType.LEFT_PARENTHESES);
        consumeToken();
        var parentheses = parseParentheses();
        if (parentheses.isEmpty()) {
            throwParserException("Expected condition");
        }
        mustBe(TokenType.RIGHT_PARENTHESES);
        consumeToken();
        var instruction = parseInstruction();
        if (instruction.isEmpty()) {
            throwParserException("Expected instruction");
        }
        return Optional.of(new While(parentheses.get(), instruction.get()));
    }

    // instruction              ::= block
    //                           | singleStatement
    //                           | compoundStatement;
    private Optional<Instruction> parseInstruction() {
        return parseBlock().map(Instruction.class::cast)
                .or(() -> parseSingleStatement().map(Instruction.class::cast))
                .or(() -> parseCompoundStatement().map(Instruction.class::cast));
    }

    // match ::= "match", "(", identifierWithValue, ")", "{", matchBranch, {matchBranch}, "}";
    private Optional<Match> parseMatch() {
        if (token.type() != TokenType.KW_MATCH) {
            return Optional.empty();
        }
        consumeToken();
        mustBe(TokenType.LEFT_PARENTHESES);
        consumeToken();
        var value = parseIdentifierWithValue();
        if (value.isEmpty()) {
            throwParserException("Expected identifier");
        }
        mustBe(TokenType.RIGHT_PARENTHESES);
        consumeToken();
        mustBe(TokenType.LEFT_CURLY_BRACKET);
        var branches = new ArrayList<MatchBranch>();
        var matchBranch = parseMatchBranch();
        while (matchBranch.isPresent()) {
            branches.add(matchBranch.get());
            matchBranch = parseMatchBranch();
        }
        mustBe(TokenType.RIGHT_CURLY_BRACKET);
        consumeToken();
        return Optional.of(new Match(branches));
    }

    // matchBranch ::= identifier, identifier, "->" instruction;
    private Optional<MatchBranch> parseMatchBranch() {
        if (token.type() != TokenType.IDENTIFIER) {
            return Optional.empty();
        }
        var type = (String) token.value();
        consumeToken();
        mustBe(TokenType.IDENTIFIER);
        var identifier = (String) token.value();
        consumeToken();
        mustBe(TokenType.ARROW);
        consumeToken();
        var instruction = parseInstruction();
        if (instruction.isEmpty()) {
            throwParserException("Expected instruction");
        }
        return Optional.of(new MatchBranch(type, identifier, instruction.get()));
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
            operator = parseMultiplicativeOperator();
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

    // factor                   ::= number
    //                           | identifierWithValue
    //                           | "(", parenthases, ")";
    private Optional<Factor> parseFactor() {
        var factor = parseNumber().map(Factor.class::cast)
                .or(() ->parseIdentifierWithValue().map(Factor.class::cast));
        if (factor.isPresent()) {
            return factor;
        }
        if (token.type() != TokenType.LEFT_PARENTHESES) {
            return Optional.empty();
        }
        consumeToken();
        var parenthases = parseParentheses();
        if (parenthases.isEmpty()) {
            throwParserException("Expected boolean value");
        }
        mustBe(TokenType.RIGHT_PARENTHESES);
        consumeToken();
        return parenthases.map(Factor.class::cast);
    }

    // identifierWithValue ::= identifier, [identifierValueApplier]
    private Optional<IdentifierWithValue> parseIdentifierWithValue() {
        if (token.type() != TokenType.IDENTIFIER) {
            return Optional.empty();
        }
        var identifier = (String) token.value();
        consumeToken();
        return Optional.of(new IdentifierWithValue(identifier, parseIdentifierValueApplier()));
    }

    // identifierValueApplier   ::= functionArguments
    //                            | as;
    private Optional<IdentifierValueApplier> parseIdentifierValueApplier() {
        return parseFunctionCall().map(IdentifierValueApplier.class::cast)
                .or(() -> parseAs().map((IdentifierValueApplier.class::cast)));
    }

    // if ::= "if" "(" parentheses ")" instruction [ "else" instruction ];
    private Optional<If> parseIf() {
        if (token.type() != TokenType.KW_IF) {
            return Optional.empty();
        }
        consumeToken();
        mustBe(TokenType.LEFT_PARENTHESES);
        consumeToken();
        var parentheses = parseParentheses();
        if (parentheses.isEmpty()) {
            throwParserException("Expected condition");
        }
        mustBe(TokenType.RIGHT_PARENTHESES);
        consumeToken();
        var instruction = parseInstruction();
        if (instruction.isEmpty()) {
            throwParserException("Expected instruction");
        }
        if (token.type() == TokenType.KW_ELSE) {
            consumeToken();
            var elseInstruction = parseInstruction();
            if (elseInstruction.isEmpty()) {
                throwParserException("Expected instruction");
            }
            return Optional.of(new If(parentheses.get(), instruction.get(), elseInstruction));
        }
        return Optional.of(new If(parentheses.get(), instruction.get(), Optional.empty()));
    }

    // parentheses ::= subcondition, {" and ", subcondition};
    private Optional<Parentheses> parseParentheses() {
        var subconditions = new ArrayList<Subcondition>();
        var subcondition = parseSubcondition();
        if (subcondition.isEmpty()) {
            return Optional.empty();
        }
        subconditions.add(subcondition.get());
        while (token.type() == TokenType.KW_AND) {
            consumeToken();
            subcondition = parseSubcondition();
            if (subcondition.isEmpty()) {
                throwParserException("Expected condition");
            }
            subconditions.add(subcondition.get());
        }
        return Optional.of(new Parentheses(subconditions));
    }

    // subcondition ::= booleanExpression, {" or ", booleanExpression};
    private Optional<Subcondition> parseSubcondition() {
        var booleanExpressions = new ArrayList<BooleanExpression>();
        var expression = parseBooleanExpression();
        if (expression.isEmpty()) {
            return Optional.empty();
        }
        booleanExpressions.add(expression.get());
        while (token.type() == TokenType.KW_OR) {
            consumeToken();
            expression = parseBooleanExpression();
            if (expression.isEmpty()) {
                throwParserException("Expected expression");
            }
            booleanExpressions.add(expression.get());
        }
        return Optional.of(new Subcondition(booleanExpressions));
    }

    // booleanExpression ::= ["!"], logicTerm;
    private Optional<BooleanExpression> parseBooleanExpression() {
        var negated = token.type() == TokenType.NEGATION_OPERATOR;
        Optional<LogicTerm> logicTerm;
        if (!negated) {
            logicTerm = parseLogicTerm();
            if (logicTerm.isEmpty()) {
                return Optional.empty();
            }
        } else {
            consumeToken();
            logicTerm = parseLogicTerm();
            if (logicTerm.isEmpty()) {
                throwParserException("Expected condition");
            }
        }
        return Optional.of(new BooleanExpression(logicTerm.get(), negated));
    }

    // logicTerm                ::= booleanLiteral
    //                            | relation;
    private Optional<LogicTerm> parseLogicTerm() {
        return parseBooleanLiteral().map(LogicTerm.class::cast)
                .or(() -> parseRelation().map(LogicTerm.class::cast));
    }

    // relation ::= expression, [arithemticCondition, expression];
    private Optional<Relation> parseRelation() {
        var first = parseExpression();
        if (first.isEmpty()) {
            return Optional.empty();
        }
        var arithmeticCondition = parseArithmeticCondition();
        if (arithmeticCondition.isEmpty()) {
            return Optional.of(new Relation(first.get(), Optional.empty(), Optional.empty()));
        }
        var second = parseExpression();
        if (second.isEmpty()) {
            throwParserException("Expected expression");
        }
        return Optional.of(new Relation(first.get(), arithmeticCondition, second));
    }

    // arithmeticCondition ::= equal
    //                       | notEqual
    //                       | lessThan
    //                       | greaterThan
    //                       | lessThanOrEqual
    //                       | greaterThanOrEqual;
    private Optional<ArithmeticCondition> parseArithmeticCondition() {
        if (!TokenTypeGroupsProvider.ARITHMETIC_CONDITION_TYPES.contains(token.type())) {
            return Optional.empty();
        }
        var type = ArithmeticCondition.fromTokenType(token.type());
        consumeToken();
        return type;
    }

    // functionArguments ::= "(", [ value { "," value } ], ")";
    private Optional<FunctionCall> parseFunctionCall() {
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
        mustBe(TokenType.RIGHT_PARENTHESES);
        consumeToken();
        return Optional.of(new FunctionCall(values));
    }

    // value ::= expression
    //         | inplaceValue;
    private Optional<Value> parseValue() {
        return parseExpression().map(Value.class::cast)
                .or(() -> parseInplaceValue().map(Value.class::cast));
    }

    // inplaceValue             ::= number
    //                           | stringLiteral
    //                           | booleanLiteral;
    private Optional<InplaceValue> parseInplaceValue() {
        return parseNumber().map(InplaceValue.class::cast)
                .or(() -> parseStringLiteral().map(InplaceValue.class::cast))
                .or(() -> parseBooleanLiteral().map(InplaceValue.class::cast));
    }

    // as ::= "as", variableType;
    private Optional<As> parseAs() {
        if (token.type() != TokenType.KW_AS) {
            return Optional.empty();
        }
        consumeToken();
        var variableType = parseVariableType();
        if (variableType.isEmpty()) {
            throwParserException("Expected variable type");
        }
        return Optional.of(new As(variableType.get()));
    }

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
        var variableType = parseVariableType();
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
    private Optional<VariableType> parseVariableType() {
        if (TokenTypeGroupsProvider.VAR_TYPES.contains(token.type())) {
            var type = token.type();
            consumeToken();
            return Optional.of(VariableType.fromTokenType(type));
        }
        return Optional.empty();
    }

    // number ::= intConst | floatConst;
    private Optional<Number> parseNumber() {
        if (token.type() == TokenType.INT_CONST) {
            var value = (int) token.value();
            consumeToken();
            return Optional.of(new IntConst(value)).map(Number.class::cast);
        }
        if (token.type() == TokenType.FLOAT_CONST) {
            var value = (float) token.value();
            consumeToken();
            return Optional.of(new FloatConst(value)).map(Number.class::cast);
        }
        return Optional.empty();
    }

    // booleanLiteral ::= "true" | "false";
    private Optional<BooleanLiteral> parseBooleanLiteral() {
        if (token.type() == TokenType.KW_TRUE) {
            consumeToken();
            return Optional.of(new BooleanLiteral(true));
        }
        if (token.type() == TokenType.KW_FALSE) {
            consumeToken();
            return Optional.of(new BooleanLiteral(false));
        }
        return Optional.empty();
    }

    private Optional<StringLiteral> parseStringLiteral() {
        if (token.type() == TokenType.STRING_CONST) {
            var value = (String) token.value();
            consumeToken();
            return Optional.of(new StringLiteral(value));
        }
        return Optional.empty();
    }

    private void mustBe(TokenType tokenType) {
        if (token.type() != tokenType) {
            throwParserException("Invalid token at row: %d, col: %d".formatted(token.row(), token.col()));
        }
    }

    private void consumeToken() {
        token = analyzer.getNextToken();
    }

    private void throwParserException(String message) {
        throw new ParserException(message, token.row(), token.col());
    }

}
