package org.example;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import pl.interpreter.Interpreter;
import pl.interpreter.executor.exceptions.InterpretationException;
import pl.interpreter.lexical_analyzer.LexicalAnalyzerException;
import pl.interpreter.parser.ParserException;

// TODO: refactor this
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            log("You must specify filepath");
            return;
        }
        var arguments = Arrays.asList(args);
        var functionToRun = "main";
        List<String> functionArguments = List.of();
        if (args.length > 1) {
            functionToRun = args[1];
            functionArguments = arguments.subList(2, args.length);
        }
        var sourceFilePath = Path.of(arguments.getFirst());
        var output = new StringWriter();
        try (var reader = Files.newBufferedReader(sourceFilePath)) {
            var interpreter = new Interpreter(reader, output);
            var returned = interpreter.run(functionToRun, functionArguments);
            System.out.println(output);
            System.out.println("Program returned: " + returned);
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            System.out.println(output);
        } catch (LexicalAnalyzerException e) {
            System.err.println("Lexical error at line %d, col %d: %s".formatted(e.getErrorRow(), e.getErrorCol(), e.getMessage()));
            System.out.println(output);
        } catch (ParserException e) {
            System.err.println("Syntax error at line %d, col %d: %s".formatted(e.getErrorRow(), e.getErrorCol(), e.getMessage()));
            System.out.println(output);
        } catch (InterpretationException e) {
            var pos = e.getPosition();
            System.err.println("Semantic error at line %d, col %d: %s".formatted(pos.row(), pos.col(), e.getMessage()));
            System.out.println(output);
        }
    }
    private static void log(String message) {
        System.out.println(message);
    }
}