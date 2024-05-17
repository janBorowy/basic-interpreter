package pl.interpreter.executor;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import lombok.Getter;
import pl.interpreter.executor.built_in_functions.BuiltInFunctionRegistry;
import pl.interpreter.executor.exceptions.EnvironmentException;
import pl.interpreter.executor.exceptions.FunctionCallException;
import pl.interpreter.parser.Definition;
import pl.interpreter.parser.FunctionDefinition;
import pl.interpreter.parser.Program;
import pl.interpreter.parser.StructureDefinition;
import pl.interpreter.parser.VariantDefinition;

public class Environment {

    private final Map<String, Function> functions;
    private final Stack<CallContext> callContexts;
    @Getter
    private final Writer standardOutput;

    public Environment(Program program) {
        this(program, new StringWriter());
    }

    public Environment(Program program, Writer standardOutputWriter) {
        functions = new HashMap<>();
        callContexts = new Stack<>();
        loadBuiltInFunctions();
        loadDefinitions(program);
        this.standardOutput = standardOutputWriter;
    }

    public CallContext getCurrentContext() {
        return callContexts.peek();
    }

    public void pushNewContext() {
        callContexts.push(new CallContext(new ArrayList<>()));
    }

    public void popContext() {
        callContexts.pop();
    }

    public Function getFunction(String functionId) {
        var function = functions.get(functionId);
        if (function == null) {
            throw new FunctionCallException("Function \"" + functionId + "\" does not exist");
        }
        return function;
    }

    public Value runFunction(String functionId, List<Value> arguments) {
        return new FunctionExecutor(this).executeFunction(functionId, arguments);
    }

    private void loadDefinitions(Program program) {
        program.getDefinitions()
                .values()
                .forEach(this::loadDefinition);
    }

    private void loadBuiltInFunctions() {
        functions.putAll(BuiltInFunctionRegistry.prepareBuiltInFunctionsForEnvironment(this));
    }

    private void loadDefinition(Definition d) {
        switch (d) {
            case FunctionDefinition fd -> registerFunction(d.getId(), UserFunctionDefinitionMapper.map(fd));
            case StructureDefinition sd -> registerFunction(sd.getId(), StructureToFunctionMapper.map(sd));
            case VariantDefinition vd -> {/*TODO: add variants support*/}
            default -> throw new IllegalStateException("Unimplemented definition: " + d);
        }
    }

    private void registerFunction(String id, Function function) {
        if (functions.containsKey(id)) {
            throw new EnvironmentException("Function with name \"" + id + "\" already exists");
        }
        functions.put(id, function);
    }
}
