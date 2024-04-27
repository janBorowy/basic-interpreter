package pl.interpreter

import pl.interpreter.lexical_analyzer.LexicalAnalyzer

import pl.interpreter.parser.ExpressionParser
import pl.interpreter.parser.PrintVisitor
import pl.interpreter.parser.TokenManager;
import spock.lang.Specification;

class ExpressionParserSpec extends Specification {

    def getExpressionParser(code) {
        var reader = new StringReader(code);
        var lexer = new LexicalAnalyzer(reader);
        var tokenManager = new TokenManager(lexer);
        return new ExpressionParser(tokenManager);
    }

    def treeStr(code) {
        var parser = getExpressionParser(code)
        var statement = parser.parseExpression().get()
        var writer = new StringWriter();
        (new PrintVisitor(writer)).visit(statement)
        return writer.toString();
    }

    def "Should parse literal"() {
        expect:
            treeStr("true") == "BooleanLiteral <row: 1, col: 1> value=true\n"
            treeStr("1") == "IntLiteral <row: 1, col: 1> value=1\n"
    }

    def "Should parse conjunction"() {
        expect:
            treeStr("false and 1") ==
"""Conjunction <row: 1, col: 1> 
|-BooleanLiteral <row: 1, col: 1> value=false
|-IntLiteral <row: 1, col: 11> value=1
"""
            treeStr("true and 5.5 or 1 and false") ==
"""Conjunction <row: 1, col: 1> 
|-Conjunction <row: 1, col: 1> 
  |-BooleanLiteral <row: 1, col: 1> value=true
  |-Alternative <row: 1, col: 10> 
    |-FloatLiteral <row: 1, col: 10> value=5.5
    |-IntLiteral <row: 1, col: 17> value=1
|-BooleanLiteral <row: 1, col: 23> value=false
"""
    }

    def "Should parse alternative"() {
        expect:
            treeStr("true") == "BooleanLiteral <row: 1, col: 1> value=true\n"
            treeStr("true or true") ==
"""Alternative <row: 1, col: 1> 
|-BooleanLiteral <row: 1, col: 1> value=true
|-BooleanLiteral <row: 1, col: 9> value=true
"""
            treeStr("true or false or true") ==
"""Alternative <row: 1, col: 1> 
|-Alternative <row: 1, col: 1> 
  |-BooleanLiteral <row: 1, col: 1> value=true
  |-BooleanLiteral <row: 1, col: 9> value=false
|-BooleanLiteral <row: 1, col: 18> value=true
"""
    }

    def "Should parse relation"() {
        expect:
            treeStr("2 > 1") ==
"""Relation <row: 1, col: 1> operator=>
|-IntLiteral <row: 1, col: 1> value=2
|-IntLiteral <row: 1, col: 5> value=1
"""
            treeStr("2 + 2 == 4") ==
"""Relation <row: 1, col: 1> operator===
|-Sum <row: 1, col: 1> operator="+"
  |-IntLiteral <row: 1, col: 1> value=2
  |-IntLiteral <row: 1, col: 5> value=2
|-IntLiteral <row: 1, col: 10> value=4
"""
    }

    def "Should parse cast"() {
        expect:
            treeStr("2 as string") == "Cast <row: 1, col: 1> type=string\n|-IntLiteral <row: 1, col: 1> value=2\n"
            treeStr("2.0 as string") == "Cast <row: 1, col: 1> type=string\n|-FloatLiteral <row: 1, col: 1> value=2.0\n"
            treeStr("2 + 2 as string") == """Cast <row: 1, col: 1> type=string
|-Sum <row: 1, col: 1> operator="+"
  |-IntLiteral <row: 1, col: 1> value=2
  |-IntLiteral <row: 1, col: 5> value=2
"""
            treeStr("str as int") == """Cast <row: 1, col: 1> type=int
|-Identifier <row: 1, col: 1> id=str
"""
            treeStr("\"hello\" as int") == """Cast <row: 1, col: 1> type=int
|-StringLiteral <row: 1, col: 1> value=hello
"""
    }

    def "Should parse sum"() {
        expect:
            treeStr("2 + 2") == """Sum <row: 1, col: 1> operator="+"
|-IntLiteral <row: 1, col: 1> value=2
|-IntLiteral <row: 1, col: 5> value=2
"""
            treeStr("1 + 2 - 3") == """Sum <row: 1, col: 1> operator="-"
|-Sum <row: 1, col: 1> operator="+"
  |-IntLiteral <row: 1, col: 1> value=1
  |-IntLiteral <row: 1, col: 5> value=2
|-IntLiteral <row: 1, col: 9> value=3
"""
    }

    def "Should parse multiplication"() {
        expect:
            treeStr("2 * 2") == """Multiplication <row: 1, col: 1> operator="*"
|-IntLiteral <row: 1, col: 1> value=2
|-IntLiteral <row: 1, col: 5> value=2
"""
            treeStr("1 * 2 / 3 % 4") == """Multiplication <row: 1, col: 1> operator="%"
|-Multiplication <row: 1, col: 1> operator="/"
  |-Multiplication <row: 1, col: 1> operator="*"
    |-IntLiteral <row: 1, col: 1> value=1
    |-IntLiteral <row: 1, col: 5> value=2
  |-IntLiteral <row: 1, col: 9> value=3
|-IntLiteral <row: 1, col: 13> value=4
"""
    }

    def "Should parse negation"() {
        expect:
            treeStr("!true") == """Negation <row: 1, col: 1> 
|-BooleanLiteral <row: 1, col: 2> value=true
"""
            treeStr("!isActive") == """Negation <row: 1, col: 1> 
|-Identifier <row: 1, col: 2> id=isActive
"""
            treeStr("!(a == 5)") == """Negation <row: 1, col: 1> 
|-Relation <row: 1, col: 3> operator="=="
  |-Identifier <row: 1, col: 3> id=a
  |-IntLiteral <row: 1, col: 8> value=5
"""
    }

    def """Should parse factor"""() {
        expect:
            treeStr("a") == "Identifier <row: 1, col: 1> id=a\n"
            treeStr("a.b") == """DotAccess <row: 1, col: 1> field_name=b
|-Identifier <row: 1, col: 1> id=a
"""
            treeStr("a()") == "FunctionCall <row: 1, col: 1> function_id=a\n"
            treeStr("a().b") == """DotAccess <row: 1, col: 1> field_name=b
|-FunctionCall <row: 1, col: 1> function_id=a
"""
            treeStr("(2 + 2)") == """Sum <row: 1, col: 2> operator="+"
|-IntLiteral <row: 1, col: 2> value=2
|-IntLiteral <row: 1, col: 6> value=2
"""
            treeStr("(2 + 2) * 3") == """Multiplication <row: 1, col: 1> operator="*"
|-Sum <row: 1, col: 2> operator="+"
  |-IntLiteral <row: 1, col: 2> value=2
  |-IntLiteral <row: 1, col: 6> value=2
|-IntLiteral <row: 1, col: 11> value=3
"""
            treeStr("!(a > b and c <= d())") == """Negation <row: 1, col: 1> 
|-Conjunction <row: 1, col: 3> 
  |-Relation <row: 1, col: 3> operator=">"
    |-Identifier <row: 1, col: 3> id=a
    |-Identifier <row: 1, col: 7> id=b
  |-Relation <row: 1, col: 13> operator="<="
    |-Identifier <row: 1, col: 13> id=c
    |-FunctionCall <row: 1, col: 18> function_id=d
"""
            treeStr("(a + b) > c") == """Relation <row: 1, col: 1> operator=">"
|-Sum <row: 1, col: 2> operator="+"
  |-Identifier <row: 1, col: 2> id=a
  |-Identifier <row: 1, col: 6> id=b
|-Identifier <row: 1, col: 11> id=c
"""
    }
}
