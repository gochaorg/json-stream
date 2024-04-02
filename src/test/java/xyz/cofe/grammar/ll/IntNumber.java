package xyz.cofe.grammar.ll;

import xyz.cofe.grammar.ll.bind.TermBind;
import xyz.cofe.grammar.ll.lexer.Matched;

import java.util.Optional;

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
