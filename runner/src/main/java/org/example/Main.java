package org.example;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import pl.interpreter.lexical_analyzer.LexicalAnalyzer;
import pl.interpreter.parser.Parser;
import pl.interpreter.parser.PrintTreeVisitor;

public class Main {
    /**
     * Print AST of standard input
     * @param args
     */
    public static void main(String[] args) {
        var reader = new StringReader("int main(int a, int b) { int a = 0; int b = 0; print(a + b); return;}");
        var analyzer = new LexicalAnalyzer(reader);
        var parser = new Parser(analyzer);
        var tree = parser.parseProgram();
        var stringWriter = new StringWriter();
        new PrintTreeVisitor(stringWriter).visit(tree);
        System.out.print(stringWriter);
    }
}