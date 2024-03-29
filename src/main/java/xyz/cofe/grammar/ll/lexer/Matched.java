package xyz.cofe.grammar.ll.lexer;

import java.util.Objects;

public record Matched<R>(R result, String source, int begin, int end) {
    public Matched {
        Objects.requireNonNull(result);
        Objects.requireNonNull(source);
        if (begin > end) throw new IllegalArgumentException("begin>end");
    }
}
