package pl.interpreter

import pl.interpreter.executor.Environment
import pl.interpreter.executor.StringValue
import pl.interpreter.executor.StructureValue
import pl.interpreter.executor.VariantValue
import pl.interpreter.executor.exceptions.AssignmentException
import pl.interpreter.executor.exceptions.InitializationException
import pl.interpreter.lexical_analyzer.LexicalAnalyzer
import pl.interpreter.parser.Program
import pl.interpreter.parser.ProgramParser
import pl.interpreter.parser.TokenManager
import spock.lang.Specification

class UserFunctionCallingVisitorFunctionalSpec extends Specification {

    Program parseProgram(String code) {
        return new ProgramParser(new TokenManager(new LexicalAnalyzer(new StringReader(code)))).parse()
    }

    Environment prepareEnvironment(String code, Writer standardOutput) {
        return new Environment(parseProgram(code), standardOutput)
    }

    def "Should initialize variable correctly"() {
        var writer = new StringWriter()
        var environment = prepareEnvironment(
                """
int initializeA() {
int a = 5;
print((a as string) + "\\n");
return a;
}
"""
                , writer)
        environment.runFunction("initializeA", List.of())
        expect:
        writer.toString() == "5\n";
    }

    def "Should throw when reinitializing variable"() {
        when:
        var writer = new StringWriter()
        var environment = prepareEnvironment(
                """
int initializeA() {
int a = 5;
int a = 3;
print((a as string) + "\\n");
return a;
}
"""
                , writer)
        environment.runFunction("initializeA", List.of())
        then:
        InitializationException e = thrown()
    }

    def "Should throw when initializing variable with incorrect type"() {
        when:
        var writer = new StringWriter()
        var environment = prepareEnvironment(
                """
int initializeA() {
int a = "a";
print((a as string) + "\\n");
return a;
}
"""
                , writer)
        environment.runFunction("initializeA", List.of())
        then:
        InitializationException e = thrown()
    }

    def "Should assign a correctly"() {
        var writer = new StringWriter()
        var environment = prepareEnvironment(
                """
int main() {
var int a = 5;
a = 3;
print((a as string) + "\\n");
return a;
}
"""
                , writer)
        environment.runFunction("main", List.of())
        expect:
        writer.toString() == "3\n";
    }

    def "Should throw when assigning uninitialized variable"() {
        when:
        var writer = new StringWriter()
        var environment = prepareEnvironment(
                """
int main() {
a = 3;
print((a as string) + "\\n");
return a;
}
"""
                , writer)
        environment.runFunction("main", List.of())
        then:
        AssignmentException e = thrown()
    }

    def "Should throw when assigning variable with incorrect value type"() {
        when:
        var writer = new StringWriter()
        var environment = prepareEnvironment(
                """
int main() {
var int a = 5;
a = "abc";
print((a as string) + "\\n");
return a;
}
"""
                , writer)
        environment.runFunction("main", List.of())
        then:
        AssignmentException e = thrown()
    }

    def "Should define structures correctly"() {
        var writer = new StringWriter()
        var environment = prepareEnvironment(
                """
struct Person {
    string name,
    string surname
}

struct Book {
    Person author,
    string title
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
""",
                writer
        )
        environment.runFunction("printPublication", List.of(new VariantValue("Publication", new StructureValue("Book", Map.of(
                "author", new StructureValue("Person", Map.of("name", new StringValue("James"), "surname", new StringValue("Black"))),
                "title", new StringValue("my story")
        )))))
        expect:
        writer.toString() == ""
    }
}
