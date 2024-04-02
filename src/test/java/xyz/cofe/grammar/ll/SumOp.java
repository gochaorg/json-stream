package xyz.cofe.grammar.ll;

import xyz.cofe.grammar.ll.bind.TermBind;
import xyz.cofe.grammar.ll.lexer.Matched;

import java.util.Optional;

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
