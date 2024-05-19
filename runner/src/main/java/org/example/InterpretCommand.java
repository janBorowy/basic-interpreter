package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.*;
import pl.interpreter.Interpreter;
import pl.interpreter.executor.exceptions.InterpretationException;
import pl.interpreter.lexical_analyzer.LexicalAnalyzerException;
import pl.interpreter.parser.ParserException;

@Command(name = "student", version = "student 1.0", mixinStandardHelpOptions = true)
public class InterpretCommand implements Callable<Integer> {

    @Option(names = {"-f", "--file"}, description = "File to interpret", required = true)
    private File file;

    @Option(names = {"-m", "--main"}, description = "Main function", required = false, defaultValue = "main")
    private String functionToRun;

    @Option(names = {"-p", "--parameters"}, split = ",")
    private String[] functionArgument;
    private final Writer output;

    public InterpretCommand() {
        output = new StringWriter();
    }

    @Override
    public Integer call() {
        try {
            return openFileAndInterpret();
        } catch (IOException e) {
            System.err.println("IO error: %s".formatted(e.getMessage()));
        }
        return -1;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new InterpretCommand()).execute(args);
        System.exit(exitCode);
    }

    private int openFileAndInterpret() throws IOException {
        try (var reader = new BufferedReader(new FileReader(file))) {
            var interpreter = new Interpreter(reader, output);
            if (Objects.isNull(functionArgument)) {
                functionArgument = new String[0];
            }
            var returned = interpreter.run(functionToRun, Arrays.asList(functionArgument));
            printOutputIfNotEmpty();
            if (Objects.nonNull(returned)) {
                System.out.println("Program returned: " + returned);
            }
            return 0;
        } catch (IOException e) {
            printOutputIfNotEmpty();
            System.err.println("IOException: " + e.getMessage());
            return 1;
        } catch (LexicalAnalyzerException e) {
            printOutputIfNotEmpty();
            System.err.println("Lexical error at line %d, col %d: %s".formatted(e.getErrorRow(), e.getErrorCol(), e.getMessage()));
            return 1;
        } catch (ParserException e) {
            printOutputIfNotEmpty();
            System.err.println("Syntax error at line %d, col %d: %s".formatted(e.getErrorRow(), e.getErrorCol(), e.getMessage()));
            return 1;
        } catch (InterpretationException e) {
            var pos = e.getPosition();
            printOutputIfNotEmpty();
            System.err.println("Semantic error at line %d, col %d: %s".formatted(pos.row(), pos.col(), e.getMessage()));
            return 1;
        }
    }

    private void printOutputIfNotEmpty() {
        var str = output.toString();
        if (!str.isEmpty()) {
            System.out.println(str);
        }
    }
}
