package xyz.cofe.json.stream.token;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Tuple2;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static xyz.cofe.json.stream.token.TokenParsers.expect;
import static xyz.cofe.json.stream.token.TokenParsers.join;
import static xyz.cofe.json.stream.token.TokenParsers.or;

/**
 * <pre>
 *
 * number      ::= [ unary_minus ] integer | float
 *
 * integer     ::= octal_int | hex_int | bin_int | dec_int
 * octal_int   ::= '0' [ 'o' | 'O' ] { octal_digit } [ 'n' ]
 * hex_int     ::= '0' ( 'x' | 'X' ) { hex_digit } [ 'n' ]
 * bin_int     ::= '0' ( 'b' | 'B' ) { bin_digit } [ 'n' ]
 * dec_int     ::= dec_digit { dec_digit } [ 'n' ]
 *
 * bin_digit   ::= '0' | '1'
 * octal_digit ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7'
 * dec_digit   ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
 * hex_digit   ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
 * | 'a' | 'b' | 'c' | 'd' | 'e' | 'f'
 * | 'A' | 'B' | 'C' | 'D' | 'E' | 'F'
 *
 * float         ::= dec_part '.' fraction_part ( 'e' | 'E' ) [ '-' | '+' ] exponent_part
 * | dec_part '.' fraction_part
 * | dec_part '.' ( 'e' | 'E' )  [ '-' | '+' ] exponent_part
 * | dec_part '.'
 * | '.' fraction_part ( 'e' | 'E' )  [ '-' | '+' ] exponent_part
 * | '.' fraction_part
 *
 * dec_part      ::= dec_digit { dec_digit }
 * fraction_part ::= dec_digit { dec_digit }
 * exponent_part ::= dec_digit { dec_digit }
 *
 * </pre>
 *
 * @param <S>
 */
public class NumberParser<S extends CharPointer<S>> implements TokenParser<S> {
    @Override
    public Optional<Token<S>> parse(S ptr) {
        if (ptr == null) throw new IllegalArgumentException("ptr==null");

        var begin = ptr;

        var signTup = sign(ptr).orElse(Tuple2.of(1, ptr));
        int sign = signTup._1();
        ptr = signTup._2();

        var rawFloat = floatParse(ptr);
        if (rawFloat.isPresent()) {
            return rawFloat.map(
                rawFloatSTuple2 ->
                    new DoubleToken<>(rawFloatSTuple2._1().toDouble(), begin, rawFloatSTuple2._2()));
        }

        var pptr = ptr;
        var rawInt = prefInt(pptr, 8, 'o', 'O')
            .or(() -> prefInt(pptr, 16, 'x', 'X'))
            .or(() -> prefInt(pptr, 2, 'b', 'B'))
            .or(() -> decInt(pptr));
        if (rawInt.isPresent()) {
            var end = rawInt.get()._2();
            var pref = rawInt.get()._1().toPreferenceNumber(sign);
            if( pref instanceof RawInt.PreferenceNumber.Long lng){
                return Optional.of(new LongToken<>(lng.number(), begin, end));
            } else if( pref instanceof RawInt.PreferenceNumber.Int int0){
                return Optional.of(new IntToken<>(int0.number(), begin, end));
            } else if( pref instanceof RawInt.PreferenceNumber.Big big){
                return Optional.of(new BigIntToken<>(big.number(), begin, end));
            } else if( pref instanceof RawInt.PreferenceNumber.OutOfLong lng2){
                return Optional.of(new BigIntToken<>(lng2.number(), begin, end));
            }
        }

        return Optional.empty();
    }

    /**
     * Сырое значение
     * @param value сырое значение
     */
    public record RawFloat(String value) {
        public RawFloat {
            Objects.requireNonNull(value);
        }

        public double toDouble() {
            return Double.parseDouble(value);
        }
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private Optional<Tuple2<RawFloat, S>> floatParse(S ptr) {
        Function<S, Optional<Tuple2<String, S>>> dec = this::dec_part;
        Function<S, Optional<Tuple2<String, S>>> frac = dec;
        Function<S, Optional<Tuple2<String, S>>> exp = dec;
        Function<S, Optional<Tuple2<String, S>>> dot = p1 -> expect(p1, ".", false);
        Function<S, Optional<Tuple2<String, S>>> e = p1 -> expect(p1, "e", true);
        Function<S, Optional<Tuple2<String, S>>> sign = p1 -> expect(p1, "+", false).or(() -> expect(p1, "-", false));

        Function<S, Optional<Tuple2<String, S>>> dec_dot_frac_e_sign_exp = join(dec, join(dot, join(frac, join(e, join(sign, exp)))));
        Function<S, Optional<Tuple2<String, S>>> dec_dot_frac_e_exp = join(dec, join(dot, join(frac, join(e, exp))));
        Function<S, Optional<Tuple2<String, S>>> dec_dot_frac = join(dec, join(dot, frac));
        Function<S, Optional<Tuple2<String, S>>> dec_dot_e_sign_exp = join(dec, join(dot, join(e, join(sign, exp))));
        Function<S, Optional<Tuple2<String, S>>> dec_dot_e_exp = join(dec, join(dot, join(e, exp)));
        Function<S, Optional<Tuple2<String, S>>> dec_dot = join(dec, dot);

        Function<S, Optional<Tuple2<String, S>>> dot_frac_e_sign_exp = join(dot, join(frac, join(e, join(sign, exp))));
        Function<S, Optional<Tuple2<String, S>>> dot_frac_e_exp = join(dot, join(frac, join(e, exp)));
        Function<S, Optional<Tuple2<String, S>>> dot_frac = join(dot, frac);

        var parseFun = or(
            dec_dot_frac_e_sign_exp,
            dec_dot_frac_e_exp,
            dec_dot_frac,
            dec_dot_e_sign_exp,
            dec_dot_e_exp,
            dec_dot,
            dot_frac_e_sign_exp,
            dot_frac_e_exp,
            dot_frac
        );

        var r = parseFun.apply(ptr).map(tup -> Tuple2.of(new RawFloat(tup._1()), tup._2()));
        return r;
    }

    private Optional<Tuple2<String, S>> dec_part(S ptr) {
        var c0 = ptr.get(0).flatMap(c -> {
            var d = Digit.digit(c, 10);
            return d < 0 ? Optional.empty() : Optional.of(c);
        });

        if (c0.isEmpty()) return Optional.empty();
        var p = ptr.move(1);

        StringBuilder sb = new StringBuilder();
        sb.append(c0.get());

        while (true) {
            var c1 = p.get(0).flatMap(c -> {
                var d = Digit.digit(c, 10);
                return d < 0 ? Optional.empty() : Optional.of(c);
            });
            if (c1.isEmpty()) break;

            sb.append(c1.get());
            p = p.move(1);
        }

        return Optional.of(Tuple2.of(sb.toString(), p));
    }

    /**
     * Сырое значение, целое
     * @param digits цифры
     * @param base системы счисления
     * @param bigInt признак bigint
     */
    public record RawInt(ImList<Integer> digits, int base, boolean bigInt) {
        public RawInt {
            Objects.requireNonNull(digits);
            if (digits.size() < 1) throw new IllegalArgumentException("digits.size()<1");

            if (base < 2) throw new IllegalArgumentException("base<2");
            if (base > 16) throw new IllegalArgumentException("base>16");
        }

        /**
         * Возвращает BigInt
         * @return значение
         */
        public BigInteger toBigInteger() {
            BigInteger num = BigInteger.ZERO;
            BigInteger kof = BigInteger.ONE;
            BigInteger base1 = BigInteger.valueOf(base);

            return digits.foldRight(Tuple2.of(num, kof), (acc, it) ->
                {
                    var n = acc._1();
                    var k = acc._2();
                    var i = BigInteger.valueOf(it);
                    var r = i.multiply(k).add(n);
                    var k2 = k.multiply(base1);
                    return Tuple2.of(r, k2);
                }
            ).map((a, b) -> a);
        }

        /**
         * Предпочтительное представление
         */
        public sealed interface PreferenceNumber {
            record Int(int number) implements PreferenceNumber {}
            record Long(long number) implements PreferenceNumber {}
            record Big(BigInteger number) implements PreferenceNumber {}
            record OutOfLong(BigInteger number) implements PreferenceNumber {}
        }

        /**
         * Выбирает подходящее компактное представление не теряя в точности
         * @param sign признак отрицательного значения
         * @return предпочтительное представление
         */
        public PreferenceNumber toPreferenceNumber(int sign) {
            var bigNum = toBigInteger();
            if (sign < 0) bigNum = BigInteger.ZERO.subtract(bigNum);

            if (this.bigInt) return new PreferenceNumber.Big(bigNum);

            if (sign < 0) {
                if (bigNum.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0)
                    return new PreferenceNumber.Int(bigNum.intValue());
                if (bigNum.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0)
                    return new PreferenceNumber.Long(bigNum.longValue());
                return new PreferenceNumber.OutOfLong(bigNum);
            } else {
                if (bigNum.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0)
                    return new PreferenceNumber.Int(bigNum.intValue());
                if (bigNum.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0)
                    return new PreferenceNumber.Long(bigNum.longValue());
                return new PreferenceNumber.OutOfLong(bigNum);
            }
        }
    }

    private Optional<Tuple2<RawInt, S>> prefInt(S ptr, int base, char pref1, char pref2) {
        return ptr.get(0).flatMap(c0 -> ptr.get(1).flatMap(c1 -> {
            if (c0 != '0' || !(c1 == pref1 || c1 == pref2)) return Optional.empty();

            var digits = ImList.<Integer>of();
            var p = ptr.move(2);
            while (true) {
                var digit = p.get(0).flatMap(c -> {
                    var d = Digit.digit(c, base);
                    return d < 0 ? Optional.empty() : Optional.of(d);
                });
                if (digit.isPresent()) {
                    digits = digits.prepend(digit.get());
                    p = p.move(1);
                } else {
                    break;
                }
            }

            var pp = p;
            var suffTup = p.get(0).flatMap(c -> c == 'n' ?
                Optional.of(Tuple2.of(true, pp.move(1))) : Optional.empty()).orElse(Tuple2.of(false, p)
            );

            p = suffTup._2();
            var big = suffTup._1();

            return Optional.of(
                Tuple2.of(new RawInt(digits.reverse(), base, big), p)
            );
        }));
    }

    private Optional<Tuple2<RawInt, S>> decInt(S ptr) {
        return ptr.get(0).flatMap(c0 -> ptr.get(1).flatMap(c1 -> {
            var d0 = ptr.get(0).flatMap(c -> {
                var d = Digit.digit(c, 10);
                return d < 0 ? Optional.empty() : Optional.of(d);
            });
            if (d0.isEmpty()) return Optional.empty();

            var digits = ImList.<Integer>of(d0.get());
            var p = ptr.move(1);
            while (true) {
                var digit = p.get(0).flatMap(c -> {
                    var d = Digit.digit(c, 10);
                    return d < 0 ? Optional.empty() : Optional.of(d);
                });
                if (digit.isPresent()) {
                    digits = digits.prepend(digit.get());
                    p = p.move(1);
                } else {
                    break;
                }
            }

            var pp = p;
            var suffTup = p.get(0).flatMap(c -> c == 'n' ? Optional.of(Tuple2.of(true, pp.move(1))) : Optional.empty()).orElse(Tuple2.of(false, p));
            p = suffTup._2();
            var big = suffTup._1();

            return Optional.of(
                Tuple2.of(new RawInt(digits.reverse(), 10, big), p)
            );
        }));
    }

    private Optional<Tuple2<Integer, S>> sign(S ptr) {
        return ptr.get(0).flatMap(c -> c == '-' ? Optional.of(Tuple2.of(-1, ptr.move(1))) : Optional.empty());
    }
}
