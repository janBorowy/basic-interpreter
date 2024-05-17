package pl.interpreter.executor;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.interpreter.parser.Block;

@Getter
@AllArgsConstructor
public class UserFunction implements Function {

    private ValueType returnType;
    private List<FunctionParameter> parameters;
    private Block block;
}
