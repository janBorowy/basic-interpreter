package pl.interpreter;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import lombok.Getter;
import pl.interpreter.executor.BooleanValue;
import pl.interpreter.executor.Environment;
import pl.interpreter.executor.FloatValue;
import pl.interpreter.executor.IntValue;
import pl.interpreter.executor.StringValue;
import pl.interpreter.executor.Value;
import pl.interpreter.executor.exceptions.FunctionCallException;
import pl.interpreter.executor.exceptions.InterpretationException;
import pl.interpreter.lexical_analyzer.LexicalAnalyzer;
import pl.interpreter.parser.Position;
import pl.interpreter.parser.Program;
import pl.interpreter.parser.ProgramParser;
import pl.interpreter.parser.TokenManager;

public class Interpreter {
    @Getter
    private final Writer output;
    private final Program program;

    public Interpreter(Reader source, Writer output) {
        this.output = output;
        this.program = new ProgramParser(new TokenManager(new LexicalAnalyzer(source))).parse();
    }

    public Value run(String mainFunctionId, List<String> arguments) {
        try {
            var environment = new Environment(program, output);
            return environment.runFunction(mainFunctionId, parseArguments(arguments));
        } catch(FunctionCallException e) {
            throw new InterpretationException(e.getMessage(), new Position(0,0));
        }
    }

    private List<Value> parseArguments(List<String> arguments) {
        return arguments.stream()
                .map(this::parseSingleArgument)
                .toList();
    }

    private Value parseSingleArgument(String argument) {
        if (argument.matches("^[-+]?[1-9]\\d*$")) {
            return new IntValue(Integer.parseInt(argument));
        }
        if (argument.matches("^[-+]?\\d*\\.?\\d+$")) {
            return new FloatValue(Float.parseFloat(argument));
        }
        if (argument.equals("false") || argument.equals("true")) {
            return new BooleanValue(argument.equals("true"));
        }
        return new StringValue(argument);
    }
}
