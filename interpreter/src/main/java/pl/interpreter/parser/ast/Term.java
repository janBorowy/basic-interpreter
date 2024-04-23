package pl.interpreter.parser.ast;

import java.util.List;
import pl.interpreter.parser.NodeVisitor;

// factors are separated by operators, for example: 2 * 2 / 3 = Expression[terms:[2, 2, 3], operators:["*", "/"]]
public record Term(List<Factor> factors, List<MultiplicativeOperator> operators, int row, int col) implements Node {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
