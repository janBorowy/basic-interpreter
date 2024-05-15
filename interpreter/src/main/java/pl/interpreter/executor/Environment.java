package pl.interpreter.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.IntStream;
import pl.interpreter.executor.exceptions.EnvironmentException;
import pl.interpreter.executor.exceptions.InvalidFunctionCallException;
import pl.interpreter.parser.Definition;
import pl.interpreter.parser.FunctionDefinition;
import pl.interpreter.parser.Program;
import pl.interpreter.parser.StructureDefinition;
import pl.interpreter.parser.VariantDefinition;

public class Environment {

    private final Map<String, Function> functions;
    private final Stack<CallContext> callContexts;

    public Environment(Program program) {
        functions = new HashMap<>();
        callContexts = new Stack<>();
        loadDefinitions(program);
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

    public Value executeUserFunction(String functionId, List<Value> arguments) {
        var function = functions.get(functionId);
        if (function == null) {
            throw new InvalidFunctionCallException("Function \"" + functionId + "\" does not exist");
        }
        if (!(function instanceof UserFunction) ) {
            throw new InvalidFunctionCallException(functionId + " is not a function");
        }
        var userFunction = (UserFunction) function;
        pushNewContext();
        var returnValue = executeFunctionBody(userFunction, arguments);
        popContext();
        return returnValue;
    }

    private Value executeFunctionBody(UserFunction function, List<Value> arguments) {
        getCurrentContext().openNewScope();
        validateFunctionArguments(function.getParameters(), arguments);
        var visitor = new UserFunctionCallingVisitor(this);
        visitor.visit(function.getBlock());
        getCurrentContext().closeClosestScope();
        return visitor.getReturnedValue();
    }

    private void validateFunctionArguments(List<FunctionParameter> parameters, List<Value> arguments) {
        var isValid = IntStream.range(0, parameters.size())
                .mapToObj(i -> parameters.get(i).valueType().typeOf(arguments.get(i)))
                .allMatch(Boolean::booleanValue);
        if (!isValid) {
            throw new InvalidFunctionCallException("Argument types do not match parameters");
        }
    }

    private void loadDefinitions(Program program) {
        program.getDefinitions()
                .values()
                .forEach(this::loadDefinition);
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
