package pl.interpreter.parser;

import pl.interpreter.parser.ast.AdditiveOperator;

public interface NodeVisitor {

    void visit(AdditiveOperator additiveOperator);

}
