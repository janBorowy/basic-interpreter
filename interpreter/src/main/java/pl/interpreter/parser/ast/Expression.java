package pl.interpreter.parser.ast;

import java.util.List;

// terms are separated by operators, for example: 2 + 2 - 3 = Expression[terms:[2, 2, 3], operators:["+", "-"]]
public record Expression(List<Term> terms, List<AdditiveOperator> operators) implements Value, ParenthesesValue {}
