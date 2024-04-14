package pl.interpreter.parser.ast;

import java.util.List;

// factors are separated by operators, for example: 2 * 2 / 3 = Expression[terms:[2, 2, 3], operators:["*", "/"]]
public record Term(List<Factor> factors, List<MultiplicativeOperator> operators) implements Node { }
