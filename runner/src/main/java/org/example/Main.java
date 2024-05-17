package org.example;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import pl.interpreter.Interpreter;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            log("You must specify filepath");
            return;
        }
        var arguments = Arrays.asList(args);
        var sourceFilePath = Path.of(arguments.getFirst());
        try (var reader = Files.newBufferedReader(sourceFilePath)) {
            var output = new StringWriter();
            var interpreter = new Interpreter(reader, output);
            interpreter.run("main", List.of());
            System.out.println(output);
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }
    private static void log(String message) {
        System.out.println(message);
    }
}