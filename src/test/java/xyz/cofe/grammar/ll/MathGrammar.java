package xyz.cofe.grammar.ll;

import xyz.cofe.grammar.ll.bind.Rule;
import xyz.cofe.grammar.ll.bind.TermBind;
import xyz.cofe.grammar.ll.bind.Terms;

import java.util.List;
import java.util.Optional;

@Terms({
    IntNumber.class,
    SumOp.class,
    MulOp.class,
    Parentheses.class,
    Whitespace.class,
})

public class MathGrammar {
    public record PlusRepeat(SumOp op, Expr right) {
        @Rule
        public static PlusRepeat parse(SumOp op, MultipleOperation right){
            return new PlusRepeat(op, right);
        }
    }

    public record PlusOperation(Expr left, Optional<SumOp> op, Optional<Expr> right) implements Expr {
//        @Rule(order = 0)
//        public static PlusOperation parse(MultipleOperation left, SumOp op, Expr right) {
//            return new PlusOperation(left, Optional.ofNullable(op), Optional.ofNullable(right));
//        }

        @Rule(order = 5)
        public static PlusOperation parse(MultipleOperation left, List<PlusRepeat> tail) {
            var head = new PlusOperation(left, Optional.empty(), Optional.empty());;
            if( tail.isEmpty() ){
                return head;
            }

            var res = head;
            for( var right : tail ){
                res = new PlusOperation(res, Optional.of(right.op()), Optional.of(right.right()));
            }

            return res;
        }

        @Rule(order = 10)
        public static PlusOperation parse(MultipleOperation left) {
            return new PlusOperation(left, Optional.empty(), Optional.empty());
        }

        @Override
        public String toString() {
            if (op().isPresent() && right.isPresent()) {
                return "( " + left + (
                    switch (op.get()) {
                        case Plus -> " + ";
                        case Minus -> " - ";
                    }
                ) + right.get() + " )";
            }

            return left.toString();
        }
    }

    public record MultipleRepeat(MulOp op, Expr right) {
        @Rule
        public static MultipleRepeat parse(MulOp op, Atom right){
            return new MultipleRepeat(op, right);
        }
    }

    public record MultipleOperation(Expr left, Optional<MulOp> op, Optional<Expr> right) implements Expr {
//        @Rule(order = 0)
//        public static MultipleOperation parse(Atom left, MulOp op, Expr right) {
//            return new MultipleOperation(left, Optional.ofNullable(op), Optional.ofNullable(right));
//        }

        @Rule(order = 5)
        public static MultipleOperation parse(Atom left, List<MultipleRepeat> tail) {
            var head = new MultipleOperation(left, Optional.empty(), Optional.empty());;
            if( tail.isEmpty() ){
                return head;
            }

            var res = head;
            for( var right : tail ){
                res = new MultipleOperation(res, Optional.of(right.op()), Optional.of(right.right()));
            }

            return res;
        }

        @Rule(order = 10)
        public static MultipleOperation parse(Atom left) {
            return new MultipleOperation(left, Optional.empty(), Optional.empty());
        }

        @Override
        public String toString() {
            if (op().isPresent() && right.isPresent()) {
                return "( "+left + (
                    switch (op.get()) {
                        case Div -> " / ";
                        case Mul -> " * ";
                    }
                ) + right.get() + " )";
            }

            return left.toString();
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

        @Override
        public String toString() {
            return switch (value) {
                case IntNumber n -> n.toString();
                default -> "(" + value.toString() + ")";
            };
        }
    }
}
