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
        default -> error("unknown date format");
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
      |-MatchBranch <row: 7, col: 9> type=null, field_name=null
        |-FunctionCall <row: 7, col: 25> function_id=error
          |-StringLiteral <row: 7, col: 26> value=unknown date format
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

    def "Should parse demo source code"() {
        expect:
        treeStr("""
/*
    This is an
    example of
    multiline comment
*/
int main() { // main is an entry point of every program
    int a = 2; // Initialize immutable integer
    int b = 2;
    int sum = a + b; // Binary(two argument) addition function
    string str = (2 + 2) as string; // immutable string initialization
    print(str); // Built-in standard output function
    
    return 0; // main return exit code
}
""") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 7, col: 1> id=main, return_type=int
  |-Block <row: 7, col: 12> 
    |-Initialization <row: 8, col: 5> id=a, var=false, type=int
      |-IntLiteral <row: 8, col: 13> value=2
    |-Initialization <row: 9, col: 5> id=b, var=false, type=int
      |-IntLiteral <row: 9, col: 13> value=2
    |-Initialization <row: 10, col: 5> id=sum, var=false, type=int
      |-Sum <row: 10, col: 15> operator="+"
        |-Identifier <row: 10, col: 15> id=a
        |-Identifier <row: 10, col: 19> id=b
    |-Initialization <row: 11, col: 5> id=str, var=false, type=string
      |-Cast <row: 11, col: 18> type=string
        |-Sum <row: 11, col: 19> operator="+"
          |-IntLiteral <row: 11, col: 19> value=2
          |-IntLiteral <row: 11, col: 23> value=2
    |-FunctionCall <row: 12, col: 10> function_id=print
      |-Identifier <row: 12, col: 11> id=str
    |-ReturnStatement <row: 14, col: 5> 
      |-IntLiteral <row: 14, col: 12> value=0
"""
        treeStr("""int main () {
    int a = 2;
    var int b = 2;
    // a = 3; ERROR!
    b = 3;
    return 0;
}
""") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 1, col: 1> id=main, return_type=int
  |-Block <row: 1, col: 13> 
    |-Initialization <row: 2, col: 5> id=a, var=false, type=int
      |-IntLiteral <row: 2, col: 13> value=2
    |-Initialization <row: 3, col: 5> id=b, var=true, type=int
      |-IntLiteral <row: 3, col: 17> value=2
    |-Assignment <row: 5, col: 7> id=b
      |-IntLiteral <row: 5, col: 9> value=3
    |-ReturnStatement <row: 6, col: 5> 
      |-IntLiteral <row: 6, col: 12> value=0
"""
        treeStr("""int main () {
    int a = 2;
    int b = 3;
    if(a % 2 == 0) {
        print("a variable's value is even");
    } else {
        print("a variable's value is uneven");
    }
    return 0;
}
""") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 1, col: 1> id=main, return_type=int
  |-Block <row: 1, col: 13> 
    |-Initialization <row: 2, col: 5> id=a, var=false, type=int
      |-IntLiteral <row: 2, col: 13> value=2
    |-Initialization <row: 3, col: 5> id=b, var=false, type=int
      |-IntLiteral <row: 3, col: 13> value=3
    |-IfStatement <row: 4, col: 5> 
      |-Relation <row: 4, col: 8> operator="=="
        |-Multiplication <row: 4, col: 8> operator="%"
          |-Identifier <row: 4, col: 8> id=a
          |-IntLiteral <row: 4, col: 12> value=2
        |-IntLiteral <row: 4, col: 17> value=0
      |-Block <row: 4, col: 20> 
        |-FunctionCall <row: 5, col: 14> function_id=print
          |-StringLiteral <row: 5, col: 15> value=a variable's value is even
      |-Block <row: 6, col: 12> 
        |-FunctionCall <row: 7, col: 14> function_id=print
          |-StringLiteral <row: 7, col: 15> value=a variable's value is uneven
    |-ReturnStatement <row: 9, col: 5> 
      |-IntLiteral <row: 9, col: 12> value=0
"""

        treeStr("""int main() {
    int i = 0;
    while(i < 10) {
        print(i as string);
        i = i + 1;
    }
    return 0;
}
""") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 1, col: 1> id=main, return_type=int
  |-Block <row: 1, col: 12> 
    |-Initialization <row: 2, col: 5> id=i, var=false, type=int
      |-IntLiteral <row: 2, col: 13> value=0
    |-WhileStatement <row: 3, col: 5> 
      |-Relation <row: 3, col: 11> operator="<"
        |-Identifier <row: 3, col: 11> id=i
        |-IntLiteral <row: 3, col: 15> value=10
      |-Block <row: 3, col: 19> 
        |-FunctionCall <row: 4, col: 14> function_id=print
          |-Cast <row: 4, col: 15> type=string
            |-Identifier <row: 4, col: 15> id=i
        |-Assignment <row: 5, col: 11> id=i
          |-Sum <row: 5, col: 13> operator="+"
            |-Identifier <row: 5, col: 13> id=i
            |-IntLiteral <row: 5, col: 17> value=1
    |-ReturnStatement <row: 7, col: 5> 
      |-IntLiteral <row: 7, col: 12> value=0
"""

        treeStr("""struct Point {
    float x,
    float y
}

int main() {
    Point point = Point(1, 2);
    // p.x = 2; ERROR!
    print(p.x); // p.x is read-only 
    return 0;
}
""") == """Program <row: 1, col: 1> 
|-StructureDefinition <row: 1, col: 1> id=Point
  |-Parameter id=x, type=float
  |-Parameter id=y, type=float
|-FunctionDefinition <row: 6, col: 1> id=main, return_type=int
  |-Block <row: 6, col: 12> 
    |-Initialization <row: 7, col: 11> id=point, var=false, type=user_type, user_type=Point
      |-FunctionCall <row: 7, col: 19> function_id=Point
        |-IntLiteral <row: 7, col: 25> value=1
        |-IntLiteral <row: 7, col: 28> value=2
    |-FunctionCall <row: 9, col: 10> function_id=print
      |-DotAccess <row: 9, col: 11> field_name=x
        |-Identifier <row: 9, col: 11> id=p
    |-ReturnStatement <row: 10, col: 5> 
      |-IntLiteral <row: 10, col: 12> value=0
"""

        treeStr("""struct Person {
    string name,
    string surname
}

struct Book {
    Person author,
    string title
}
""") == """Program <row: 1, col: 1> 
|-StructureDefinition <row: 1, col: 1> id=Person
  |-Parameter id=surname, type=string
  |-Parameter id=name, type=string
|-StructureDefinition <row: 6, col: 1> id=Book
  |-Parameter id=author, type=user_type, userType=Person
  |-Parameter id=title, type=string
"""

        treeStr("""struct Person {
    string name,
    string surname
}

struct Book {
    string title,
    string isbn,
    Person author
}

struct Article {
    string headline,
    string shownIn,
    Person author
}

variant Publication {
    Book,
    Article
}

void printPublication(Publication pub) {
    match(pub) {
        Book book -> print("Book with title - " + book.title);
        Article article -> print("Article with headline - " + article.headline);
        default -> print("Unknown publication");
    }
}
""") == """Program <row: 1, col: 1> 
|-StructureDefinition <row: 1, col: 1> id=Person
  |-Parameter id=surname, type=string
  |-Parameter id=name, type=string
|-StructureDefinition <row: 6, col: 1> id=Book
  |-Parameter id=author, type=user_type, userType=Person
  |-Parameter id=isbn, type=string
  |-Parameter id=title, type=string
|-StructureDefinition <row: 12, col: 1> id=Article
  |-Parameter id=author, type=user_type, userType=Person
  |-Parameter id=headline, type=string
  |-Parameter id=shownIn, type=string
|-VariantDefinition <row: 18, col: 1> id=Publication
  |-Type id=Book
  |-Type id=Article
|-FunctionDefinition <row: 23, col: 1> id=printPublication, return_type=void
  |-Parameter id=pub, type=user_type, userType=Publication
  |-Block <row: 23, col: 40> 
    |-MatchStatement <row: 24, col: 5> 
      |-Identifier <row: 24, col: 11> id=pub
      |-MatchBranch <row: 25, col: 9> type=Book, field_name=book
        |-FunctionCall <row: 25, col: 27> function_id=print
          |-Sum <row: 25, col: 28> operator="+"
            |-StringLiteral <row: 25, col: 28> value=Book with title - 
            |-DotAccess <row: 25, col: 51> field_name=title
              |-Identifier <row: 25, col: 51> id=book
      |-MatchBranch <row: 26, col: 9> type=Article, field_name=article
        |-FunctionCall <row: 26, col: 33> function_id=print
          |-Sum <row: 26, col: 34> operator="+"
            |-StringLiteral <row: 26, col: 34> value=Article with headline - 
            |-DotAccess <row: 26, col: 63> field_name=headline
              |-Identifier <row: 26, col: 63> id=article
      |-MatchBranch <row: 27, col: 9> type=null, field_name=null
        |-FunctionCall <row: 27, col: 25> function_id=print
          |-StringLiteral <row: 27, col: 26> value=Unknown publication
"""

        treeStr("""struct IntPoint {
    int ix,
    int iy
}

struct FloatPoint {
    float fx,
    float fy
}

variant Point {
    IntPoint,
    FloatPoint
}

float getCoordinatesSum(Point p) {
    match(p) {
        IntPoint ip -> {
            int sum = ip.ix + ip.iy;
            return sum as float; 
        }
        FloatPoint fp -> return fp.fx + fp.fy;
    }
}
""") == """Program <row: 1, col: 1> 
|-StructureDefinition <row: 1, col: 1> id=IntPoint
  |-Parameter id=iy, type=int
  |-Parameter id=ix, type=int
|-StructureDefinition <row: 6, col: 1> id=FloatPoint
  |-Parameter id=fx, type=float
  |-Parameter id=fy, type=float
|-VariantDefinition <row: 11, col: 1> id=Point
  |-Type id=IntPoint
  |-Type id=FloatPoint
|-FunctionDefinition <row: 16, col: 1> id=getCoordinatesSum, return_type=float
  |-Parameter id=p, type=user_type, userType=Point
  |-Block <row: 16, col: 34> 
    |-MatchStatement <row: 17, col: 5> 
      |-Identifier <row: 17, col: 11> id=p
      |-MatchBranch <row: 18, col: 9> type=IntPoint, field_name=ip
        |-Block <row: 18, col: 24> 
          |-Initialization <row: 19, col: 13> id=sum, var=false, type=int
            |-Sum <row: 19, col: 23> operator="+"
              |-DotAccess <row: 19, col: 23> field_name=ix
                |-Identifier <row: 19, col: 23> id=ip
              |-DotAccess <row: 19, col: 31> field_name=iy
                |-Identifier <row: 19, col: 31> id=ip
          |-ReturnStatement <row: 20, col: 13> 
            |-Cast <row: 20, col: 20> type=float
              |-Identifier <row: 20, col: 20> id=sum
      |-MatchBranch <row: 22, col: 9> type=FloatPoint, field_name=fp
        |-ReturnStatement <row: 22, col: 26> 
          |-Sum <row: 22, col: 33> operator="+"
            |-DotAccess <row: 22, col: 33> field_name=fx
              |-Identifier <row: 22, col: 33> id=fp
            |-DotAccess <row: 22, col: 41> field_name=fy
              |-Identifier <row: 22, col: 41> id=fp
"""

        treeStr("""int main() {
    int a = 2;
    if(true) {
        int a = 3;
        print(a as string); // 3
    }
    print(a as string); // 2
}
""") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 1, col: 1> id=main, return_type=int
  |-Block <row: 1, col: 12> 
    |-Initialization <row: 2, col: 5> id=a, var=false, type=int
      |-IntLiteral <row: 2, col: 13> value=2
    |-IfStatement <row: 3, col: 5> 
      |-BooleanLiteral <row: 3, col: 8> value=true
      |-Block <row: 3, col: 14> 
        |-Initialization <row: 4, col: 9> id=a, var=false, type=int
          |-IntLiteral <row: 4, col: 17> value=3
        |-FunctionCall <row: 5, col: 14> function_id=print
          |-Cast <row: 5, col: 15> type=string
            |-Identifier <row: 5, col: 15> id=a
    |-FunctionCall <row: 7, col: 10> function_id=print
      |-Cast <row: 7, col: 11> type=string
        |-Identifier <row: 7, col: 11> id=a
"""

        treeStr("""int getNthFibonacciNumber(int n) {
    if(n == 0 or n == 1) {
        return n;
    }
    return getNthFibonacciNumber(n - 1) + getNthFibonacciNumber(n - 2);
}
""") == """Program <row: 1, col: 1> 
|-FunctionDefinition <row: 1, col: 1> id=getNthFibonacciNumber, return_type=int
  |-Parameter id=n, type=int
  |-Block <row: 1, col: 34> 
    |-IfStatement <row: 2, col: 5> 
      |-Alternative <row: 2, col: 8> 
        |-Relation <row: 2, col: 8> operator="=="
          |-Identifier <row: 2, col: 8> id=n
          |-IntLiteral <row: 2, col: 13> value=0
        |-Relation <row: 2, col: 18> operator="=="
          |-Identifier <row: 2, col: 18> id=n
          |-IntLiteral <row: 2, col: 23> value=1
      |-Block <row: 2, col: 26> 
        |-ReturnStatement <row: 3, col: 9> 
          |-Identifier <row: 3, col: 16> id=n
    |-ReturnStatement <row: 5, col: 5> 
      |-Sum <row: 5, col: 12> operator="+"
        |-FunctionCall <row: 5, col: 12> function_id=getNthFibonacciNumber
          |-Sum <row: 5, col: 34> operator="-"
            |-Identifier <row: 5, col: 34> id=n
            |-IntLiteral <row: 5, col: 38> value=1
        |-FunctionCall <row: 5, col: 43> function_id=getNthFibonacciNumber
          |-Sum <row: 5, col: 65> operator="-"
            |-Identifier <row: 5, col: 65> id=n
            |-IntLiteral <row: 5, col: 69> value=2
"""
    }

}
