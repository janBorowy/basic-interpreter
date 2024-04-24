package pl.interpreter.parser;

import pl.interpreter.parser.ast.AdditiveOperator;
import pl.interpreter.parser.ast.AfterIdentifierStatement;
import pl.interpreter.parser.ast.As;
import pl.interpreter.parser.ast.Block;
import pl.interpreter.parser.ast.BooleanExpression;
import pl.interpreter.parser.ast.BooleanLiteral;
import pl.interpreter.parser.ast.CompoundStatement;
import pl.interpreter.parser.ast.Expression;
import pl.interpreter.parser.ast.FloatConst;
import pl.interpreter.parser.ast.FunctionCall;
import pl.interpreter.parser.ast.FunctionDefinition;
import pl.interpreter.parser.ast.FunctionReturnType;
import pl.interpreter.parser.ast.FunctionSignature;
import pl.interpreter.parser.ast.IdentifierStatement;
import pl.interpreter.parser.ast.IdentifierStatementApplier;
import pl.interpreter.parser.ast.IdentifierWithValue;
import pl.interpreter.parser.ast.If;
import pl.interpreter.parser.ast.Initialization;
import pl.interpreter.parser.ast.InplaceValue;
import pl.interpreter.parser.ast.IntConst;
import pl.interpreter.parser.ast.Match;
import pl.interpreter.parser.ast.MatchBranch;
import pl.interpreter.parser.ast.MultiplicativeOperator;
import pl.interpreter.parser.ast.ParameterSignature;
import pl.interpreter.parser.ast.Parentheses;
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
import pl.interpreter.parser.ast.VariantDefinition;
import pl.interpreter.parser.ast.While;

public interface NodeVisitor {

    void visit(AdditiveOperator additiveOperator);

    void visit(While aWhile);

    void visit(AfterIdentifierStatement afterIdentifierStatement);

    void visit(As as);

    void visit(Block block);

    void visit(BooleanExpression booleanExpression);

    void visit(BooleanLiteral booleanLiteral);

    void visit(FloatConst floatConst);

    void visit(FunctionCall functionCall);

    void visit(FunctionDefinition functionDefinition);

    void visit(FunctionReturnType functionReturnType);

    void visit(FunctionSignature functionSignature);

    void visit(IdentifierStatement identifierStatement);

    void visit(IdentifierWithValue identifierWithValue);

    void visit(If anIf);

    void visit(IntConst intConst);

    void visit(Match match);

    void visit(MatchBranch matchBranch);

    void visit(MultiplicativeOperator multiplicativeOperator);

    void visit(ParameterSignature parameterSignature);

    void visit(Parentheses parentheses);

    void visit(PrimitiveInitialization primitiveInitialization);

    void visit(Program program);

    void visit(Relation relation);

    void visit(Return aReturn);

    void visit(StringLiteral stringLiteral);

    void visit(StructureDefinition structureDefinition);

    void visit(Subcondition subcondition);

    void visit(Term term);

    void visit(UserTypeInitialization userTypeInitialization);

    void visit(ValueAssignment valueAssignment);

    void visit(VariableAssignment variableAssignment);

    void visit(VariantDefinition variantDefinition);

    void visit(VarInitialization varInitialization);

    void visit(Expression expression);

    void visit(SingleStatement singleStatement);

    void visit(CompoundStatement compoundStatement);

    void visit(IdentifierStatementApplier identifierStatementApplier);

    void visit(Value value);

    void visit(InplaceValue inplaceValue);

    void visit(Initialization initialization);
}
