package pl.interpreter.parser.ast;

import java.util.List;
import pl.interpreter.parser.NodeVisitor;

// terms are separated by operators, for example: 2 + 2 - 3 = Expression[terms:[2, 2, 3], operators:["+", "-"]]
public record Expression(List<Term> terms, List<AdditiveOperator> operators, int row, int col) implements Value, ParenthesesValue {
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
