package xyz.cofe.grammar.ll;

import xyz.cofe.grammar.ll.bind.TermBind;
import xyz.cofe.grammar.ll.lexer.Matched;

import java.util.Optional;

public record Ident(String id) implements Expr {
    @TermBind("id")
    public static Optional<Matched<Ident>> parse(String source, int offset) {
        if( source==null ) throw new IllegalArgumentException("source==null");
        if( offset<0 || offset>=source.length() )return Optional.empty();

        char c = source.charAt(offset);
        if( !Character.isLetter(c) )return Optional.empty();

        int begin = offset;
        StringBuilder sb = new StringBuilder();
        sb.append(c);

        offset++;
        while (offset < source.length() ){
            c = source.charAt(offset);
            if( !Character.isLetterOrDigit(c) )break;
            sb.append(c);
        }

        return Optional.of(new Matched<>(new Ident(sb.toString()),source,begin,offset));
    }
}
