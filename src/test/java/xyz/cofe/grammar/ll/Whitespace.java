package xyz.cofe.grammar.ll;

import xyz.cofe.grammar.ll.bind.TermBind;
import xyz.cofe.grammar.ll.lexer.Matched;

import java.util.Optional;

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
