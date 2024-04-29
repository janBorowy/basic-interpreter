package pl.interpreter

import pl.interpreter.parser.ExpressionParser
import pl.interpreter.parser.ParserException
import pl.interpreter.parser.PrintVisitor
import pl.interpreter.parser.TokenManager
import spock.lang.Specification;

import pl.interpreter.lexical_analyzer.LexicalAnalyzer;
import pl.interpreter.parser.SingleStatementParser;

class SingleStatementParserSpec extends Specification {

    def getSingleStatementParser(String code) {
        var reader = new StringReader(code);
        var lexer = new LexicalAnalyzer(reader);
        var tokenManager = new TokenManager(lexer);
        var expressionParser = new ExpressionParser(tokenManager);
        return new SingleStatementParser(expressionParser, tokenManager);
    }

    def treeStr(code) {
        var parser = getSingleStatementParser(code)
        var statement = parser.parseSingleStatement().get()
        var writer = new StringWriter();
        (new PrintVisitor(writer)).visit(statement)
        return writer.toString();
    }


    def "Should parse return statement"() {
        expect:
        treeStr("return;") == """ReturnStatement <row: 1, col: 1> 
"""
        treeStr("return 0;") == """ReturnStatement <row: 1, col: 1> 
|-IntLiteral <row: 1, col: 8> value=0
"""
        treeStr("return a * b;") == """ReturnStatement <row: 1, col: 1> 
|-Multiplication <row: 1, col: 8> operator="*"
  |-Identifier <row: 1, col: 8> id=a
  |-Identifier <row: 1, col: 12> id=b
"""
    }

    def "Should parse function call"() {
        expect:
        treeStr("helloWorld();") == """FunctionCall <row: 1, col: 11> function_id=helloWorld
"""
        treeStr("doSomething(2 + 2);") == """FunctionCall <row: 1, col: 12> function_id=doSomething
|-Sum <row: 1, col: 13> operator="+"
  |-IntLiteral <row: 1, col: 13> value=2
  |-IntLiteral <row: 1, col: 17> value=2
"""
        treeStr("doSomething(a, b);") == """FunctionCall <row: 1, col: 12> function_id=doSomething
|-Identifier <row: 1, col: 13> id=a
|-Identifier <row: 1, col: 16> id=b
"""
    }

    def "Should throw if no comma in function call"() {
        when:
        treeStr("doSomething(a b);")
        then:
        ParserException e = thrown()
    }

    def "Should parse assignment"() {
        expect:
        treeStr("a = 1;") == """Assignment <row: 1, col: 3> id=a
|-IntLiteral <row: 1, col: 5> value=1
"""
    }

    def "Should parse initialization"() {
        expect:
        treeStr("Cricle circle = Circle(1, 1);") == """Initialization <row: 1, col: 8> id=circle, var=false, type=user_type, user_type=Cricle
|-FunctionCall <row: 1, col: 17> function_id=Circle
  |-IntLiteral <row: 1, col: 24> value=1
  |-IntLiteral <row: 1, col: 27> value=1
"""
        treeStr("int a = 1;") == """Initialization <row: 1, col: 1> id=a, var=false, type=int
|-IntLiteral <row: 1, col: 9> value=1
"""
    }

    def "Should parse var initialization"() {
        expect:
        treeStr("var int a = 1;") == """Initialization <row: 1, col: 1> id=a, var=true, type=int
|-IntLiteral <row: 1, col: 13> value=1
"""
        treeStr("var Circle c = Circle(1);") == """Initialization <row: 1, col: 1> id=c, var=true, type=user_type, user_type=Circle
|-FunctionCall <row: 1, col: 16> function_id=Circle
  |-IntLiteral <row: 1, col: 23> value=1
"""
    }
}
