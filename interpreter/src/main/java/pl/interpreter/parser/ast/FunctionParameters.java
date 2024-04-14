package pl.interpreter.parser.ast;

import java.util.List;
import java.util.Objects;

public class FunctionParameters implements Node {

    private final List<ParameterSignature> parameterSignatures;

    public FunctionParameters(List<ParameterSignature> parameterSignatures) {
        if (Objects.isNull(parameterSignatures)) {
            this.parameterSignatures = List.of();
        } else {
            this.parameterSignatures = parameterSignatures;
        }
    }
}
