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
import pl.interpreter.executor.exceptions.ParameterTypeException;
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
        if (Objects.isNull(returnType) && Objects.isNull(typeReturned)) {
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
        validateFunctionCall(structureConstructor.getExpectedParameterTypes(), arguments);
        return new StructureValue(structureConstructor.getStructureName(), getStructureFields(structureConstructor.getFieldNames(), arguments));
    }

    private Value executeBuiltInFunction(BuiltInFunction builtInFunction, List<Value> arguments) {
        validateFunctionCall(builtInFunction.getExpectedParameterTypes(), arguments);
        try {
            return builtInFunction.execute(arguments);
        } catch (IOException e) {
            throw new StandardOutputException("Could not write to standard output: " + e.getMessage());
        }
    }

    private Value executeFunctionBody(UserFunction function, List<Value> arguments) {
        environment.getCurrentContext().openNewScope();
        var parameterValueTypes = function.getParameters().stream().map(FunctionParameter::valueType).toList();
        validateFunctionCall(parameterValueTypes, arguments);
        var visitor = new UserFunctionCallingVisitor(environment);
        visitor.visit(function.getBlock());
        environment.getCurrentContext().closeClosestScope();
        return visitor.getReturnedValue();
    }

    private void validateFunctionCall(List<ValueType> expectedParameterTypes, List<Value> arguments) {
        if (arguments.size() != expectedParameterTypes.size()) {
            throw new FunctionCallException("Expected " + expectedParameterTypes.size() + " arguments, got " + arguments.size());
        }
        if (argumentTypesDontMatch(expectedParameterTypes, arguments)) {
            throw new ParameterTypeException("Invalid parameter type");
        }
    }

    private boolean argumentTypesDontMatch(List<ValueType> expectedParameterTypes, List<Value> arguments) {
        return !IntStream.range(0, arguments.size())
                .allMatch(i -> expectedParameterTypes.get(i).isTypeOf(arguments.get(i)) &&
                        userTypesMatch(expectedParameterTypes.get(i), arguments.get(i)));
    }

    private boolean userTypesMatch(ValueType type, Value value) {
        return switch (value) {
            case StructureValue s -> Objects.equals(s.getStructureName(), type.getUserType()) || structureIsVariant(s, type.getUserType());
            case VariantValue v -> Objects.equals(v.getVariantId(), type.getUserType());
            default -> true;
        };
    }

    private boolean structureIsVariant(StructureValue value, String variantId) {
        var variant = environment.getVariant(variantId);
        if (variant.isEmpty()) {
            return false;
        }
        return variant.map(it -> it.getStructures().contains(value.getStructureName())).get();
    }

    private Map<String, Value> getStructureFields(List<String> fieldNames, List<Value> arguments) {
        var fields = new HashMap<String, Value>();
        IntStream.range(0, fieldNames.size())
                .forEach(i -> fields.put(fieldNames.get(i), arguments.get(i)));
        return fields;
    }
}
