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

    def "Should throw if duplicated parameter names"() {
        when:
        treeStr("int sum (int a, int a) { return a + a; }")
        then:
        ParserException e = thrown()
        e.getMessage() == "Parameter identifier duplicated at row: 1, col: 10"
    }

    def "Should parse compound statements"() {
        expect:
            treeStr("int main () { if (a == 1) return a; else return b; }") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 1, col: 1> id=main, return_type=int
  |-Block <row: 1, col: 13> 
    |-IfStatement <row: 1, col: 15> 
      |-Relation <row: 1, col: 19> operator="=="
        |-Identifier <row: 1, col: 19> id=a
        |-IntLiteral <row: 1, col: 24> value=1
      |-ReturnStatement <row: 1, col: 27> 
        |-Identifier <row: 1, col: 34> id=a
      |-ReturnStatement <row: 1, col: 42> 
        |-Identifier <row: 1, col: 49> id=b
"""

        treeStr("""int main () {
    if (a == b and b != 0) {
        print("Hello");
        b = b + 1;
    }
}""") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 1, col: 1> id=main, return_type=int
  |-Block <row: 1, col: 13> 
    |-IfStatement <row: 2, col: 5> 
      |-Conjunction <row: 2, col: 9> 
        |-Relation <row: 2, col: 9> operator="=="
          |-Identifier <row: 2, col: 9> id=a
          |-Identifier <row: 2, col: 14> id=b
        |-Relation <row: 2, col: 20> operator="!="
          |-Identifier <row: 2, col: 20> id=b
          |-IntLiteral <row: 2, col: 25> value=0
      |-Block <row: 2, col: 28> 
        |-FunctionCall <row: 3, col: 14> function_id=print
          |-StringLiteral <row: 3, col: 15> value=Hello
        |-Assignment <row: 4, col: 11> id=b
          |-Sum <row: 4, col: 13> operator="+"
            |-Identifier <row: 4, col: 13> id=b
            |-IntLiteral <row: 4, col: 17> value=1
"""
        treeStr("int main () { if (a > 1) {} else { a = a + 1; } }")
    }

    def "Should throw if expression is empty"() {
        when:
        treeStr("""int main() { if () print("yes"); }""")
        then:
        ParserException e = thrown()
    }

    def "Should throw if no instruction"() {
        when:
        treeStr("""int main() { if () }""")
        then:
        ParserException e = thrown()
    }

    def "Should throw if no instruction after else"() {
        when:
        treeStr("""int main(){ if(a <= 1) {} else }""")
        then:
        ParserException e = thrown()
    }

    def "Should parse while statement"() {
        expect:
            treeStr("int main () { while(1) doSomething(); }") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 1, col: 1> id=main, return_type=int
  |-Block <row: 1, col: 13> 
    |-WhileStatement <row: 1, col: 15> 
      |-IntLiteral <row: 1, col: 21> value=1
      |-FunctionCall <row: 1, col: 35> function_id=doSomething
"""
        treeStr("""
int main() {
    while (keepGoing(a)) {
        print(\"hello\");
        incrementCounter();
    }
    return 0;
}
""") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 2, col: 1> id=main, return_type=int
  |-Block <row: 2, col: 12> 
    |-WhileStatement <row: 3, col: 5> 
      |-FunctionCall <row: 3, col: 12> function_id=keepGoing
        |-Identifier <row: 3, col: 22> id=a
      |-Block <row: 3, col: 26> 
        |-FunctionCall <row: 4, col: 14> function_id=print
          |-StringLiteral <row: 4, col: 15> value=hello
        |-FunctionCall <row: 5, col: 25> function_id=incrementCounter
    |-ReturnStatement <row: 7, col: 5> 
      |-IntLiteral <row: 7, col: 12> value=0
"""
    }

    def "Should throw if no expression in while"() {
        when:
        treeStr("int main () { while() doSomething(); }")
        then:
        ParserException e = thrown()
    }

    def "Should throw if no instruction after while"() {
        when:
        treeStr("int main() { while(a) }")
        then:
        ParserException e = thrown()
    }

    def "Should parse match"() {
        expect:
        treeStr("""
string getDateString(MetaData meta) {
    var string dateStr = "";
    match(meta.date) {
        FunnyDateFormat fdf -> dateStr = parseFdf(fdf);
        SeriousDateFormat sdf -> dateStr = parseSdf(sdf);
    }
    return dateStr;
}
""") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 2, col: 1> id=getDateString, return_type=string
  |-Parameter id=meta, type=user_type, userType=MetaData
  |-Block <row: 2, col: 37> 
    |-Initialization <row: 3, col: 5> id=dateStr, var=true, type=string
      |-StringLiteral <row: 3, col: 26> value=
    |-MatchStatement <row: 4, col: 5> 
      |-DotAccess <row: 4, col: 11> field_name=date
        |-Identifier <row: 4, col: 11> id=meta
      |-MatchBranch <row: 5, col: 9> type=FunnyDateFormat, field_name=fdf
        |-Assignment <row: 5, col: 40> id=dateStr
          |-FunctionCall <row: 5, col: 42> function_id=parseFdf
            |-Identifier <row: 5, col: 51> id=fdf
      |-MatchBranch <row: 6, col: 9> type=SeriousDateFormat, field_name=sdf
        |-Assignment <row: 6, col: 42> id=dateStr
          |-FunctionCall <row: 6, col: 44> function_id=parseSdf
            |-Identifier <row: 6, col: 53> id=sdf
"""
        treeStr("""
string getDateString(MetaData meta) {
    var string dateStr = "";
    match(meta.date) {
        FunnyDateFormat fdf -> dateStr = parseFdf(fdf);
    }
    return dateStr;
}
""") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 2, col: 1> id=getDateString, return_type=string
  |-Parameter id=meta, type=user_type, userType=MetaData
  |-Block <row: 2, col: 37> 
    |-Initialization <row: 3, col: 5> id=dateStr, var=true, type=string
      |-StringLiteral <row: 3, col: 26> value=
    |-MatchStatement <row: 4, col: 5> 
      |-DotAccess <row: 4, col: 11> field_name=date
        |-Identifier <row: 4, col: 11> id=meta
      |-MatchBranch <row: 5, col: 9> type=FunnyDateFormat, field_name=fdf
        |-Assignment <row: 5, col: 40> id=dateStr
          |-FunctionCall <row: 5, col: 42> function_id=parseFdf
            |-Identifier <row: 5, col: 51> id=fdf
"""
    }

    def "Should throw in no branches in match"() {
        when:
        treeStr("int main () { match(a) { } }")
        then:
        ParserException e = thrown()
    }
}
