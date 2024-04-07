package xyz.cofe.json.stream.token;

import xyz.cofe.coll.im.Tuple2;

import java.util.Optional;

/**
 * <pre>
 * string ::= singe_quoted_string | double_quoted_string
 * singe_quoted_string  ::= '\'' { encoded_char } '\''
 * double_quoted_string ::= '"' { encoded_char } '"'
 * encoded_char ::= escaped_seq | simple_char
 * escaped_seq ::= escape_hex | escape_unicode_ext | escape_unicode | escape_oct | escape_simple
 * escape_oct ::= '\' oct_char oct_char oct_char
 * escape_simple ::= '\' ( '0' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' | '\'' | '"' | '\' )
 * escape_hex ::= '\x' hex_char hex_char
 * escape_unicode ::= '\&#x0075;' hex_char hex_char hex_char hex_char
 * escape_unicode_ext ::= '\&#x0075;' '{' hex_char hex_char hex_char hex_char hex_char '}'
 * </pre>
 *
 * @param value
 * @param begin
 * @param end
 * @param <S>
 */
public record StringToken<S extends CharPointer<S>>(
    String value,
    S begin,
    S end
) implements Token<S> {
    public static class Parser<S extends CharPointer<S>> implements TokenParser<S> {
        @Override
        public Optional<StringToken<S>> parse(S ptr) {
            if (ptr == null) throw new IllegalArgumentException("ptr==null");
            return switch (expectQuote(ptr)) {
                case 1 -> quotedString(ptr, 1);
                case 2 -> quotedString(ptr, 2);
                default -> Optional.empty();
            };
        }

        private int expectQuote(S ptr) {
            return ptr.get(0).map(c ->
                c == '\''
                    ? 1 :
                    c == '"' ? 2 : -1
            ).orElse(-1);
        }

        private Optional<StringToken<S>> quotedString(S ptr, int quoteType) {
            S begin = ptr;

            StringBuilder buff = new StringBuilder();
            ptr = ptr.move(1);

            while (true) {
                var chr = encoded_char(ptr, quoteType);
                if (chr.isPresent()) {
                    var c = chr.get()._1();
                    ptr = chr.get()._2();
                    buff.append(c);
                } else {
                    var quote = expectQuote(ptr);
                    if (quote == quoteType) {
                        return Optional.of(new StringToken<>(buff.toString(), begin, ptr.move(1)));
                    }else {
                        return Optional.empty();
                    }
                }
            }
        }

        private Optional<Tuple2<Character, S>> encoded_char(S ptr, int quoteType) {
            return escaped_seq(ptr).or(() -> ptr.get(0).flatMap(c -> switch (c) {
                case '\'' ->
                    quoteType == 1 ? Optional.<Tuple2<Character, S>>empty() : Optional.of(Tuple2.of('\'', ptr.move(1)));
                case '"' ->
                    quoteType == 2 ? Optional.<Tuple2<Character, S>>empty() : Optional.of(Tuple2.of('"', ptr.move(1)));
                default -> Optional.of(Tuple2.of(c, ptr.move(1)));
            }));
        }

        private Optional<Tuple2<Character, S>> escaped_seq(S ptr) {
            return escape_hex(ptr)
                .or(() -> escape_unicode_ext(ptr))
                .or(() -> escape_unicode(ptr))
                .or(() -> escape_oct(ptr))
                .or(() -> escape_simple(ptr))
                ;
        }

        // \ x hex hex
        private Optional<Tuple2<Character, S>> escape_hex(S ptr) {
            return
                ptr.get(0).flatMap(
                    c0 -> ptr.get(1).flatMap(
                        c1 -> ptr.get(2).flatMap(
                            c2 -> ptr.get(3).flatMap(
                                c3 -> {
                                    if (c0 != '\\') return Optional.empty();
                                    if (c1 != 'x') return Optional.empty();

                                    var d0 = Digit.digit(c2, 16);
                                    var d1 = Digit.digit(c3, 16);

                                    if (d0 < 0 || d1 < 0)
                                        return Optional.empty();

                                    var n =
                                        d0 * 16
                                            + d1;

                                    return Optional.of(Tuple2.of((char) n, ptr.move(4)));
                                }))));
        }

        // \ u { hex hex hex hex hex }
        private Optional<Tuple2<Character, S>> escape_unicode_ext(S ptr) {
            return
                ptr.get(0).flatMap(
                    c0 -> ptr.get(1).flatMap(
                        c1 -> ptr.get(2).flatMap(
                            c2 -> ptr.get(3).flatMap(
                                c3 -> ptr.get(4).flatMap(
                                    c4 -> ptr.get(5).flatMap(
                                        c5 -> ptr.get(6).flatMap(
                                            c6 -> ptr.get(6).flatMap(
                                                c7 -> ptr.get(6).flatMap(
                                                    c8 -> {
                                                        if (c0 != '\\') return Optional.empty();
                                                        if (c1 != 'u') return Optional.empty();
                                                        if (c2 != '{') return Optional.empty();
                                                        if (c8 != '}') return Optional.empty();

                                                        var d0 = Digit.digit(c3, 16);
                                                        var d1 = Digit.digit(c4, 16);
                                                        var d2 = Digit.digit(c5, 16);
                                                        var d3 = Digit.digit(c6, 16);
                                                        var d4 = Digit.digit(c7, 16);

                                                        if (d0 < 0 || d1 < 0 || d2 < 0 || d3 < 0 || d4 < 0)
                                                            return Optional.empty();

                                                        var n =
                                                            d0 * 16 * 16 * 16 * 16
                                                                + d1 * 16 * 16 * 16
                                                                + d2 * 16 * 16
                                                                + d3 * 16
                                                                + d4;

                                                        return Optional.of(Tuple2.of((char) n, ptr.move(9)));
                                                    })))))))));
        }

        // \ u hex hex hex hex
        private Optional<Tuple2<Character, S>> escape_unicode(S ptr) {
            return
                ptr.get(0).flatMap(c0 -> ptr.get(1).flatMap(c1 -> ptr.get(2).flatMap(c2 -> ptr.get(3).flatMap(c3 -> ptr.get(4).flatMap(c4 -> ptr.get(5).flatMap(c5 -> {
                    if (c0 != '\\') return Optional.empty();
                    if (c1 != 'u') return Optional.empty();

                    var d0 = Digit.digit(c2, 16);
                    var d1 = Digit.digit(c3, 16);
                    var d2 = Digit.digit(c4, 16);
                    var d3 = Digit.digit(c5, 16);
                    if (d0 < 0 || d1 < 0 || d2 < 0 || d3 < 0) return Optional.empty();

                    var n = d0 * 16 * 16 * 16 + d1 * 16 * 16 + d2 * 16 + d3;
                    return Optional.of(Tuple2.of((char) n, ptr.move(6)));
                }))))));
        }

        private Optional<Tuple2<Character, S>> escape_oct(S ptr) {
            return ptr.get(0).flatMap(c0 -> ptr.get(1).flatMap(c1 -> ptr.get(2).flatMap(c2 -> ptr.get(3).flatMap(c3 -> {
                if (c0 != '\\') return Optional.empty();

                var d1 = Digit.digit(c1, 8);
                var d2 = Digit.digit(c2, 8);
                var d3 = Digit.digit(c3, 8);
                if (d1 < 0 || d2 < 0 || d3 < 0) return Optional.empty();

                var n = d1 * 8 * 8 + d2 * 8 + d3;
                return Optional.of(Tuple2.of((char) n, ptr.move(4)));
            }))));
        }

        private Optional<Tuple2<Character, S>> escape_simple(S ptr) {
            return ptr.get(0).flatMap(c0 -> ptr.get(1).flatMap(c1 -> {
                if (c0 == '\\' && c1 == '0') return Optional.of(Tuple2.of((char) 0, ptr.move(2)));
                if (c0 == '\\' && c1 == 'b') return Optional.of(Tuple2.of((char) 8, ptr.move(2)));
                if (c0 == '\\' && c1 == 'f') return Optional.of(Tuple2.of((char) 12, ptr.move(2)));
                if (c0 == '\\' && c1 == 'n') return Optional.of(Tuple2.of((char) 10, ptr.move(2)));
                if (c0 == '\\' && c1 == 'r') return Optional.of(Tuple2.of((char) 13, ptr.move(2)));
                if (c0 == '\\' && c1 == 't') return Optional.of(Tuple2.of((char) 9, ptr.move(2)));
                if (c0 == '\\' && c1 == 'v') return Optional.of(Tuple2.of((char) 11, ptr.move(2)));
                if (c0 == '\\' && c1 == '\'') return Optional.of(Tuple2.of('\'', ptr.move(2)));
                if (c0 == '\\' && c1 == '"') return Optional.of(Tuple2.of('"', ptr.move(2)));
                if (c0 == '\\' && c1 == '\\') return Optional.of(Tuple2.of('\\', ptr.move(2)));
                return Optional.empty();
            }));
        }
    }
}
