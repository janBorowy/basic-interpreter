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

    def "Should throw if no types were specified in variant"() {
        when:
            treeStr("""variant NoType {}""")
        then:
            ParserException e = thrown()
    }

    def "Should throw if missing type after comma in variant"() {
        when:
            treeStr("""variant Point { IntPoint, }""")
        then:
            ParserException e = thrown()
    }

    def "Should throw if no comma in variant"() {
        when:
            treeStr("""variant Point { IntPoint FloatPoint }""")
        then:
            ParserException e = thrown()
    }

    def "Should parse function definition"() {
        expect:
            treeStr("int main() {}") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 1, col: 1> id=main, return_type=int
  |-Block <row: 1, col: 12> 
"""
            treeStr("int main() { return 0; }") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 1, col: 1> id=main, return_type=int
  |-Block <row: 1, col: 12> 
    |-ReturnStatement <row: 1, col: 14> 
      |-IntLiteral <row: 1, col: 21> value=0
"""
            treeStr("int main() { return 0; return 1; }") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 1, col: 1> id=main, return_type=int
  |-Block <row: 1, col: 12> 
    |-ReturnStatement <row: 1, col: 14> 
      |-IntLiteral <row: 1, col: 21> value=0
    |-ReturnStatement <row: 1, col: 24> 
      |-IntLiteral <row: 1, col: 31> value=1
"""
            treeStr("float square(float a) { return a * a; }") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 1, col: 1> id=square, return_type=float
  |-Parameter id=a, type=float
  |-Block <row: 1, col: 23> 
    |-ReturnStatement <row: 1, col: 25> 
      |-Multiplication <row: 1, col: 32> operator="*"
        |-Identifier <row: 1, col: 32> id=a
        |-Identifier <row: 1, col: 36> id=a
"""
            treeStr("string sum(int a, bool b) { return a + b as string; }") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 1, col: 1> id=sum, return_type=string
  |-Parameter id=a, type=int
  |-Parameter id=b, type=boolean
  |-Block <row: 1, col: 27> 
    |-ReturnStatement <row: 1, col: 29> 
      |-Cast <row: 1, col: 36> type=string
        |-Sum <row: 1, col: 36> operator="+"
          |-Identifier <row: 1, col: 36> id=a
          |-Identifier <row: 1, col: 40> id=b
"""
    }

    def "Should parse var initialization"() {
        expect:
        treeStr("int main() { var int a = 1; }") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 1, col: 1> id=main, return_type=int
  |-Block <row: 1, col: 12> 
    |-Initialization <row: 1, col: 14> id=a, var=true, type=int
      |-IntLiteral <row: 1, col: 26> value=1
"""
        treeStr("int main () { var Circle c = Circle(1); }") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 1, col: 1> id=main, return_type=int
  |-Block <row: 1, col: 13> 
    |-Initialization <row: 1, col: 15> id=c, var=true, type=user_type, user_type=Circle
      |-FunctionCall <row: 1, col: 30> function_id=Circle
        |-IntLiteral <row: 1, col: 37> value=1
"""
    }
}
