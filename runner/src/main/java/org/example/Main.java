package org.example;

import java.io.InputStreamReader;
import java.io.StringReader;
import pl.interpreter.Token;
import pl.interpreter.TokenType;
import pl.interpreter.lexical_analyzer.LexicalAnalyzer;
import pl.interpreter.lexical_analyzer.LexicalAnalyzerException;
import pl.interpreter.parser.Parser;
import pl.interpreter.parser.ast.PrintTreeVisitor;

public class Main {
    /**
     * Print AST of standard input
     * @param args
     */
    public static void main(String[] args) {
        var reader = new StringReader("int main() { }");
        var analyzer = new LexicalAnalyzer(reader);
        var parser = new Parser(analyzer);
        var tree = parser.parseProgram();
        new PrintTreeVisitor().visit(tree);
    }
}