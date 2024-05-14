package pl.interpreter.executor;

import lombok.AllArgsConstructor;
import pl.interpreter.parser.FunctionDefinition;

@AllArgsConstructor
public class UserFunction implements Function {

    private FunctionDefinition functionDefinition;

    @Override
    public void execute() {

    }
}
