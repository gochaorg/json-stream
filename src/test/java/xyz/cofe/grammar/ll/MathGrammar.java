package xyz.cofe.grammar.ll;

import xyz.cofe.grammar.ll.bind.Rule;
import xyz.cofe.grammar.ll.bind.TermBind;
import xyz.cofe.grammar.ll.bind.Terms;

import java.util.Optional;

@Terms({
    IntNumber.class,
    SumOp.class,
    Parentheses.class,
    Whitespace.class,
})
public class MathGrammar {

    public record PlusOperation(Expr left, Optional<SumOp> op, Optional<Expr> right) implements Expr {
        @Rule(order = 0)
        public static PlusOperation parse(MultipleOperation left, SumOp op, Expr right) {
            return new PlusOperation(left, Optional.ofNullable(op), Optional.ofNullable(right));
        }

        @Rule(order = 1)
        public static PlusOperation parse(MultipleOperation left) {
            return new PlusOperation(left, Optional.empty(), Optional.empty());
        }
    }

    public record MultipleOperation(Expr left, Optional<MulOp> op, Optional<Expr> right) implements Expr {
        @Rule(order = 0)
        public static MultipleOperation parse(Atom left, MulOp op, Expr right) {
            return new MultipleOperation(left, Optional.ofNullable(op), Optional.ofNullable(right));
        }

        @Rule(order = 1)
        public static MultipleOperation parse(Atom left) {
            return new MultipleOperation(left, Optional.empty(), Optional.empty());
        }
    }

    public record Atom(Expr value) implements Expr {
        @Rule
        public static Atom parse(IntNumber number) {
            return new Atom(number);
        }

        @Rule(order = 1)
        public static Atom parse(@TermBind("(") Parentheses left, Expr expr, @TermBind(")") Parentheses right) {
            return new Atom(expr);
        }
    }
}
