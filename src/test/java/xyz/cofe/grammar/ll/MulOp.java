package xyz.cofe.grammar.ll;

import xyz.cofe.grammar.ll.bind.TermBind;
import xyz.cofe.grammar.ll.lexer.Matched;

import java.util.Optional;

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
