package xyz.cofe.grammar.ll;

import xyz.cofe.grammar.ll.bind.Rule;
import xyz.cofe.grammar.ll.bind.TermBind;
import xyz.cofe.grammar.ll.bind.Terms;
import xyz.cofe.grammar.ll.lexer.Matched;

import java.util.Optional;

@Terms({
    MathGrammar.IntNumber.class,
    MathGrammar.SumOp.class,
    MathGrammar.Parentheses.class,
    MathGrammar.Whitespace.class,
})
public class MathGrammar {
    public sealed interface Expr {
        @Rule
        static Expr parse(PlusOperation expr) {
            return expr;
        }
    }

    //region term IntNumber
    public record IntNumber(int value) implements Expr {
        @TermBind("1")
        public static Optional<Matched<IntNumber>> parse(String source, int begin) {
            if (source == null) throw new IllegalArgumentException("source==null");
            if (begin >= source.length()) return Optional.empty();

            var ptr = begin;
            String state = null;
            StringBuilder sb = new StringBuilder();
            boolean stop = false;

            while (ptr < source.length() && !stop) {
                char c = source.charAt(ptr);
                switch (state) {
                    case null -> {
                        switch (c) {
                            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                                sb.append(c);
                                state = "d";
                                ptr += 1;
                            }
                            default -> {
                                state = "error";
                            }
                        }
                    }
                    case "d" -> {
                        switch (c) {
                            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                                sb.append(c);
                                state = "d";
                                ptr += 1;
                            }
                            default -> {
                                state = "finish";
                                stop = true;
                            }
                        }
                    }
                    default -> {
                        stop = true;
                    }
                }
            }
            state = state.equals("d") ? "finish" : state;

            if (state.equals("finish")) {
                return Optional.of(
                    new Matched<>(
                        new IntNumber(
                            Integer.parseInt(sb.toString())
                        ),
                        source,
                        begin,
                        ptr
                    )
                );
            }

            return Optional.empty();
        }
    }
    //endregion
    //region term SumOp
    public enum SumOp {
        Plus,
        Minus;

        @TermBind("+")
        public static Optional<Matched<SumOp>> parse(String source, int begin) {
            if (source == null) throw new IllegalArgumentException("source==null");
            if (begin < 0 || begin >= source.length()) return Optional.empty();
            var chr = source.charAt(begin);
            return switch (chr) {
                case '+' -> Optional.of(new Matched<>(SumOp.Plus, source, begin, begin + 1));
                case '-' -> Optional.of(new Matched<>(SumOp.Minus, source, begin, begin + 1));
                default -> Optional.empty();
            };
        }
    }
    //endregion
    //region term MulOp
    public enum MulOp {
        Div,
        Mul;

        @TermBind("*")
        public static Optional<Matched<MulOp>> parse(String source, int begin) {
            if (source == null) throw new IllegalArgumentException("source==null");
            if (begin < 0 || begin >= source.length()) return Optional.empty();
            var chr = source.charAt(begin);
            return switch (chr) {
                case '*' -> Optional.of(new Matched<>(MulOp.Mul, source, begin, begin + 1));
                case '/' -> Optional.of(new Matched<>(MulOp.Div, source, begin, begin + 1));
                default -> Optional.empty();
            };
        }
    }
    //endregion
    //region term Parentheses
    public enum Parentheses {
        @TermBind("(")
        Open,

        @TermBind(")")
        Close;
    }
    //endregion
    //region term Whitespace
    public record Whitespace() {
        @TermBind(value = "ws", skip = true)
        public static Optional<Matched<Whitespace>> parse(String source, int begin) {
            if (source == null) throw new IllegalArgumentException("source==null");
            if (begin < 0 || begin >= source.length()) return Optional.empty();

            var chr = source.charAt(begin);
            if (!Character.isWhitespace(chr)) return Optional.empty();

            var ptr = begin + 1;
            while (ptr < source.length()) {
                chr = source.charAt(ptr);
                if (!Character.isWhitespace(chr)) break;
                ptr += 1;
            }

            return Optional.of(new Matched<>(new Whitespace(), source, begin, ptr));
        }
    }
    //endregion

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
