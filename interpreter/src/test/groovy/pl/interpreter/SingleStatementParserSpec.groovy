package pl.interpreter

import pl.interpreter.parser.PrintVisitor
import spock.lang.Specification;

import pl.interpreter.lexical_analyzer.LexicalAnalyzer;
import pl.interpreter.parser.SingleStatementParser;

class SingleStatementParserSpec extends Specification {

    def getSingleStatementParser(String code) {
        var reader = new StringReader(code);
        var lexer = new LexicalAnalyzer(reader);
        return new SingleStatementParser(lexer);
    }

    def treeStr(code) {
        var parser = getSingleStatementParser("return;")
        var statement = parser.parseReturn().get()
        var writer = new StringWriter();
        (new PrintVisitor(writer)).visit(statement)
        return writer.toString();
    }


    def "Should parse empty return statement correctly"() {
        var parser = getSingleStatementParser("return;")
        var statement = parser.parseReturn().get()
        var writer = new StringWriter();
        (new PrintVisitor(writer)).visit(statement)
        expect:
            writer.toString() == "ReturnStatement <row: 1, col: 1> \n"
    }

    def "Should parse return statement with expression"() {
        var parser = getSingleStatementParser("return 1;")
        var statement = parser.parseReturn().get()
        var writer = new StringWriter()
        (new PrintVisitor(writer)).visit(statement)
        expect:
            writer.toString() == ""
    }
}
