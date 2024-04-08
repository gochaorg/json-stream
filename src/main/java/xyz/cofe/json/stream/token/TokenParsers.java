package xyz.cofe.json.stream.token;

import xyz.cofe.coll.im.Tuple2;

import java.util.Optional;
import java.util.function.Function;

public class TokenParsers {
    public static <S extends CharPointer<S>> Optional<Tuple2<String, S>> expect(S ptr, String text, boolean ignoreCase) {
        if( ptr==null ) throw new IllegalArgumentException("ptr==null");
        if( text==null ) throw new IllegalArgumentException("text==null");
        if (text.isEmpty()) return Optional.empty();

        var buff = new StringBuilder();

        for (var i = 0; i < text.length(); i++) {
            var copt = ptr.get(i);
            if (copt.isEmpty()) return Optional.empty();

            char c0 = copt.get();
            char c1 = text.charAt(i);

            var matched = ignoreCase ? Character.toLowerCase(c0) == Character.toLowerCase(c1) : c0 == c1;
            if (!matched) return Optional.empty();

            buff.append(c0);
        }

        return Optional.of(Tuple2.of(buff.toString(), ptr.move(text.length())));
    }

    public static <S extends CharPointer<S>> Function<S, Optional<Tuple2<String, S>>> join(Function<S, Optional<Tuple2<String, S>>> first, Function<S, Optional<Tuple2<String, S>>> second) {
        if( first==null ) throw new IllegalArgumentException("first==null");
        if( second==null ) throw new IllegalArgumentException("second==null");

        return ptr -> first.apply(ptr).flatMap(tup1 -> second.apply(tup1._2()).flatMap(tup2 -> Optional.of(Tuple2.of(tup1._1() + tup2._1(), tup2._2()))));
    }

    public static <S extends CharPointer<S>> Function<S, Optional<Tuple2<String, S>>> or(
        Function<S, Optional<Tuple2<String, S>>> ... parsers
    ) {
        return ptr -> {
            for (var parser : parsers) {
                var res = parser.apply(ptr);
                if(res.isPresent()) return res;
            }
            return Optional.empty();
        };
    }
}
