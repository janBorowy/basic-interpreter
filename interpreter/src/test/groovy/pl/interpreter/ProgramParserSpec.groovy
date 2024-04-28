package pl.interpreter;

import pl.interpreter.lexical_analyzer.LexicalAnalyzer;
import pl.interpreter.parser.ParserException;
import pl.interpreter.parser.PrintVisitor
import pl.interpreter.parser.ProgramParser;
import pl.interpreter.parser.TokenManager;
import spock.lang.Specification;

class ProgramParserSpec extends Specification {

    def getProgramParser(code) {
        var reader = new StringReader(code);
        var lexer = new LexicalAnalyzer(reader);
        var tokenManager = new TokenManager(lexer);
        return new ProgramParser(tokenManager);
    }

    def treeStr(code) {
        var parser = getProgramParser(code)
        var statement = parser.parseProgram()
        var writer = new StringWriter();
        (new PrintVisitor(writer)).visit(statement)
        return writer.toString();
    }

    def "Should parse structure definition"() {
        expect:
            treeStr("struct Rectangle{int a, int b}") == """Program <row: 1, col: 1> 
|-StructureDefinition <row: 1, col: 1> id=Rectangle
  |-Parameter id=a, type=int
  |-Parameter id=b, type=int
"""

            treeStr(
"""
struct Rectangle {
    float a,
    float b,
    string name,
    MetaData metaData
}""") == """Program <row: 1, col: 1> 
|-StructureDefinition <row: 2, col: 1> id=Rectangle
  |-Parameter id=a, type=float
  |-Parameter id=metaData, type=user_type, userType=MetaData
  |-Parameter id=b, type=float
  |-Parameter id=name, type=string
"""

        treeStr(
"""
struct User {
    string userName,
    string password,
    int score,
    float height,
    Hero hero,
    Stats stats
}
"""
        ) == """Program <row: 1, col: 1> 
|-StructureDefinition <row: 2, col: 1> id=User
  |-Parameter id=score, type=int
  |-Parameter id=password, type=string
  |-Parameter id=stats, type=user_type, userType=Stats
  |-Parameter id=hero, type=user_type, userType=Hero
  |-Parameter id=userName, type=string
  |-Parameter id=height, type=float
"""
        treeStr("struct Empty { }") == """Program <row: 1, col: 1> 
|-StructureDefinition <row: 1, col: 1> id=Empty
"""
    }


    def "Should parse variant definitions"() {
        expect:
            treeStr("""
variant Point {
    IntPoint,
    FloatPoint
}
""") == """Program <row: 1, col: 1> 
|-VariantDefinition <row: 2, col: 1> id=Point
  |-Type id=IntPoint
  |-Type id=FloatPoint
"""
            treeStr(
"""
variant SingleValue {
    single
}
""") == """Program <row: 1, col: 1> 
|-VariantDefinition <row: 2, col: 1> id=SingleValue
  |-Type id=single
"""
            treeStr(
""" variant SmallCaseTypes {
a,
b,
c
}
""") == """Program <row: 1, col: 1> 
|-VariantDefinition <row: 1, col: 2> id=SmallCaseTypes
  |-Type id=a
  |-Type id=b
  |-Type id=c
"""
    }

    def "Should throw if no types were specified"() {
        when:
            treeStr("""variant NoType {}""")
        then:
            ParserException e = thrown()
    }

    def "Should throw if missing type after comma"() {
        when:
        treeStr("""variant Point { IntPoint, }""")
        then:
        ParserException e = thrown()
    }

    def "Should throw if no comma"() {
        when:
        treeStr("""variant Point { IntPoint FloatPoint }""")
        then:
        ParserException e = thrown()
    }
}
