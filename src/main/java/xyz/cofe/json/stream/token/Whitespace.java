package xyz.cofe.json.stream.token;

import java.util.Optional;

public record Whitespace<S extends CharPointer<S>>(
    String value,
    S begin,
    S end
) implements Token<S> {
    public static class Parser<S extends CharPointer<S>> implements TokenParser<S> {
        @Override
        public Optional<? extends Token<S>> parse(S ptr) {
            if( ptr==null ) throw new IllegalArgumentException("ptr==null");
            var begin = ptr;

            StringBuilder buff = new StringBuilder();
            var ch = ptr.get(0).flatMap( c -> Character.isWhitespace(c) ? Optional.of(c) : Optional.empty() );
            if( ch.isEmpty() )return Optional.empty();

            buff.append(ch.get());
            ptr = ptr.move(1);
            while (true){
                ch = ptr.get(0).flatMap( c -> Character.isWhitespace(c) ? Optional.of(c) : Optional.empty() );
                if( ch.isEmpty() )break;
                buff.append(ch.get());
                ptr = ptr.move(1);
            }

            return Optional.of(new Whitespace<>(buff.toString(), begin, ptr));
        }
    }
}
