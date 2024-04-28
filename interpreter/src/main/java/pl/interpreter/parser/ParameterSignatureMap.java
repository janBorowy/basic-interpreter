package pl.interpreter.parser;

import java.util.HashMap;

public class ParameterSignatureMap extends HashMap<String, ParameterType> {

    public void add(String identifier, ParameterTypeEnum parameterType, String userType, Position position) {
        if (containsKey(identifier)) {
            throw new ParserException("Parameter identifier duplicated at row: %d, col: %d".formatted(position.row(), position.col()));
        }
        put(identifier, new ParameterType(parameterType, userType));
    }

}
