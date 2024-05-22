package pl.interpreter.executor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import pl.interpreter.executor.built_in_functions.BuiltInFunction;
import pl.interpreter.executor.exceptions.FunctionCallException;
import pl.interpreter.executor.exceptions.StandardOutputException;

@AllArgsConstructor
public class FunctionExecutor {

    private final Environment environment;

    public Value executeFunction(String functionId, List<Value> arguments) {
        var function = environment.getFunction(functionId)
                .orElseThrow(() -> new FunctionCallException("Function \"" + functionId + "\" does not exist"));
        return switch (function) {
            case UserFunction uf -> executeUserFunction(uf, arguments);
            case StructureConstructor sc -> executeStructureConstructor(sc, arguments);
            case BuiltInFunction bif -> executeBuiltInFunction(bif, arguments);
            default -> throw new IllegalStateException("Unknown function implementation: " + function);
        };
    }

    private Value executeUserFunction(UserFunction userFunction, List<Value> arguments) {
        environment.pushNewContext();
        var returnValue = executeFunctionBody(userFunction, arguments);
        environment.popContext();
        validateReturnedValue(userFunction.getReturnType(), TypeUtils.getTypeOf(returnValue));
        return returnValue;
    }

    private void validateReturnedValue(ValueType returnType, ValueType typeReturned) {
        if (Objects.isNull(returnType) && Objects.nonNull(typeReturned)) {
            throw new FunctionCallException("Function was not expected to return value, but returned " + typeReturned);
        }
        if (Objects.nonNull(returnType) && Objects.isNull(typeReturned)) {
            throw new FunctionCallException("Function did not return, but was expected to return " + returnType);
        }
        if (Objects.isNull(returnType)) {
            return;
        }
        if (returnType.getType() == ValueType.Type.USER_TYPE && typeReturned.getType() == ValueType.Type.USER_TYPE) {
            var variant = environment.getVariant(returnType.getUserType());
            if (variant.isPresent() && variant.get().getStructures().contains(typeReturned.getUserType())) {
                return;
            }
        }
        if (!returnType.equals(typeReturned)) {
            throw new FunctionCallException("Function returned \"%s\", where \"%s\" was expected".formatted(typeReturned, returnType));
        }
    }

    private Value executeStructureConstructor(StructureConstructor structureConstructor, List<Value> arguments) {
        validateFunctionCall(mapValueTypesToImmutableParameters(structureConstructor.getExpectedParameterTypes()), arguments);
        return new StructureValue(structureConstructor.getStructureName(), getStructureFields(structureConstructor.getFieldNames(), arguments));
    }

    private Value executeBuiltInFunction(BuiltInFunction builtInFunction, List<Value> arguments) {
        validateFunctionCall(mapValueTypesToImmutableParameters(builtInFunction.getExpectedParameterTypes()), arguments);
        try {
            return builtInFunction.execute(arguments);
        } catch (IOException e) {
            throw new StandardOutputException("Could not write to standard output: " + e.getMessage());
        }
    }

    private List<FunctionParameter> mapValueTypesToImmutableParameters(List<ValueType> types) {
        return types.stream()
                .map(it -> new FunctionParameter("", it, false))
                .toList();
    }

    private Value executeFunctionBody(UserFunction function, List<Value> arguments) {
        environment.getCurrentContext().openNewScope();
        var parameterValueTypes = function.getParameters();
        validateFunctionCall(parameterValueTypes, arguments);
        setFunctionArguments(function.getParameters(), arguments);
        var visitor = new UserFunctionCallingVisitor(environment);
        visitor.visit(function.getBlock());
        environment.getCurrentContext().closeClosestScope();
        return visitor.getReturnedValue();
    }

    private void validateFunctionCall(List<FunctionParameter> expectedParameterTypes, List<Value> arguments) {
        if (arguments.size() != expectedParameterTypes.size()) {
            throw new FunctionCallException("Expected " + expectedParameterTypes.size() + " arguments, got " + arguments.size());
        }
        validateArgumentsMatchParameterTypes(arguments, expectedParameterTypes);
    }

    private void validateArgumentsMatchParameterTypes(List<Value> arguments, List<FunctionParameter> expectedParameterTypes) {
        IntStream.range(0, arguments.size())
                .forEach(i -> {
                    validateSingleArgumentType(arguments.get(i), expectedParameterTypes.get(i));
                    validateSingleArgumentReference(arguments.get(i), expectedParameterTypes.get(i).isVar());
                });
    }

    private void validateSingleArgumentType(Value argument, FunctionParameter parameter) {
        if (parameter.isVar() && TypeUtils.isVariant(parameter.valueType(), environment) && !(ReferenceUtils.getReferencedValue(argument) instanceof VariantValue)) {
            throw new FunctionCallException("Expected %s, but got %s".formatted(parameter.valueType(), argument));
        }
        if (!ValueMatcher.valueMatchesType(argument, parameter.valueType(), environment)) {
            throw new FunctionCallException("Expected %s value, but was given %s".formatted(parameter.valueType(), argument));
        }
    }

    private void validateSingleArgumentReference(Value argument, boolean isVar) {
        if (isVar && argument instanceof Reference ref && !ref.isMutable()) {
            throw new FunctionCallException("Function expects mutable reference");
        }
    }

    private Map<String, Value> getStructureFields(List<String> fieldNames, List<Value> arguments) {
        var fields = new HashMap<String, Value>();
        IntStream.range(0, fieldNames.size())
                .forEach(i -> fields.put(fieldNames.get(i), arguments.get(i)));
        return fields;
    }

    private void setFunctionArguments(List<FunctionParameter> parameters, List<Value> functionArguments) {
        IntStream.range(0, parameters.size())
                .forEach(i -> initializeVariable(parameters.get(i), functionArguments.get(i)));
    }

    private void initializeVariable(FunctionParameter parameter, Value argument) {
        if (!(argument instanceof Reference)) {
            argument = argument.clone();
        }
        if (TypeUtils.isVariant(parameter.valueType(), environment)) {
            environment.getCurrentContext()
                    .initializeVariableForClosestScope(parameter.id(), new Variable(getStructAsVariant(argument, parameter), parameter.isVar()));
        } else {
            environment.getCurrentContext()
                    .initializeVariableForClosestScope(parameter.id(), new Variable(argument, parameter.isVar()));
        }
    }

    private Value getStructAsVariant(Value value, FunctionParameter parameter) {
        if (value instanceof Reference reference) {
            return new Reference(getStructAsVariant(reference.getReferencedValue(), parameter), parameter.isVar());
        }
        if (value instanceof StructureValue structureValue) {
            return new VariantValue(parameter.valueType().getUserType(), structureValue);
        }
        return value;
    }
}
