package pl.interpreter.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import pl.interpreter.parser.Definition;
import pl.interpreter.parser.FunctionDefinition;
import pl.interpreter.parser.Program;
import pl.interpreter.parser.StructureDefinition;
import pl.interpreter.parser.VariantDefinition;

public class Environment {

    private final Map<String, StructureDefinition> structures;
    private final Map<String, FunctionDefinition> functions;
    private final Map<String, VariantDefinition> variants;
    private final Stack<CallContext> callContexts;

    public Environment(Program program) {
        structures = new HashMap<>();
        functions = new HashMap<>();
        variants = new HashMap<>();
        callContexts = new Stack<>();
        loadDefinitions(program);
    }

    private void loadDefinitions(Program program) {
        program.getDefinitions()
                .values()
                .forEach(this::loadDefinition);
    }

    private void loadDefinition(Definition d) {
        switch (d) {
            case FunctionDefinition fd -> functions.put(fd.getId(), fd);
            case StructureDefinition sd -> structures.put(sd.getId(), sd);
            case VariantDefinition vd -> variants.put(vd.getId(), vd);
            default -> throw new IllegalStateException("Unimplemented definition: " + d);
        }
    }
}
