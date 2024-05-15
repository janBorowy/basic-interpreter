package pl.interpreter.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import pl.interpreter.executor.exceptions.EnvironmentException;
import pl.interpreter.parser.Definition;
import pl.interpreter.parser.FunctionDefinition;
import pl.interpreter.parser.Program;
import pl.interpreter.parser.StructureDefinition;
import pl.interpreter.parser.VariantDefinition;

public class Environment {

    private final Map<String, Function> functions;
    private final Map<String, VariantDefinition> variants;
    private final Stack<CallContext> callContexts;

    public Environment(Program program) {
        functions = new HashMap<>();
        variants = new HashMap<>();
        callContexts = new Stack<>();
        loadDefinitions(program);
    }

    public void pushNewContext() {
        callContexts.push(new CallContext(new ArrayList<>()));
    }

    public void popContext() {
        callContexts.pop();
    }

    private void loadDefinitions(Program program) {
        program.getDefinitions()
                .values()
                .forEach(this::loadDefinition);
    }

    private void loadDefinition(Definition d) {
        switch (d) {
            case FunctionDefinition fd -> registerFunction(d.getId(), UserFunctionDefinitionMapper.map(fd));
            case StructureDefinition sd -> functions.put(sd.getId(), StructureToFunctionMapper.map(sd));
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
