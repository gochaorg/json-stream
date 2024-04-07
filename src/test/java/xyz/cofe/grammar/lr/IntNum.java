package xyz.cofe.grammar.lr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IntNum implements TokenParser<Integer,StringSource> {
    private List<Integer> digits;
    private StringSource begin;
    private StringSource end;
    private int base = 10;

    public IntNum() {
        digits = new ArrayList<>();
    }

    @Override
    public void reset() {
        begin = null;
        end = null;
        digits.clear();
    }

    @Override
    public boolean hasState() {
        return !digits.isEmpty();
    }

    private static int digitOf(char chr, int base) {
        return switch (chr) {
            case '0' -> base >= 1 ? 0 : -1;
            case '1' -> base >= 2 ? 1 : -1;
            case '2' -> base >= 3 ? 2 : -1;
            case '3' -> base >= 4 ? 3 : -1;
            case '4' -> base >= 5 ? 4 : -1;
            case '5' -> base >= 6 ? 5 : -1;
            case '6' -> base >= 7 ? 6 : -1;
            case '7' -> base >= 8 ? 7 : -1;
            case '8' -> base >= 9 ? 8 : -1;
            case '9' -> base >= 10 ? 9 : -1;
            case 'a', 'A' -> base >= 11 ? 10 : -1;
            case 'b', 'B' -> base >= 12 ? 11 : -1;
            case 'c', 'C' -> base >= 13 ? 12 : -1;
            case 'd', 'D' -> base >= 14 ? 13 : -1;
            case 'e', 'E' -> base >= 15 ? 14 : -1;
            case 'f', 'F' -> base >= 16 ? 15 : -1;
            default -> -1;
        };
    }

    private Optional<Integer> number() {
        if (digits.isEmpty()) return Optional.empty();
        int sum = 0;
        int k = 1;
        for (int i = digits.size() - 1; i >= 0; i--) {
            var d = digits.get(i);
            var v = d * k;
            k = k * base;
            sum = sum + v;
        }
        return Optional.of(sum);
    }

    public Optional<ParsedToken<Integer, StringSource>> input(StringSource src) {
        if (src == null) throw new IllegalArgumentException("src==null");

        var chrOpt = src.get();
        if (chrOpt.isEmpty()) {
            return flush();
        }

        int digit = digitOf(chrOpt.get(), base);
        if (digit <= 0) {
            return flush();
        }

        if (digits.isEmpty()) {
            begin = src;
        }
        end = src;

        digits.add(digit);

        return Optional.empty();
    }

    private Optional<ParsedToken<Integer, StringSource>> flush() {
        var res = number();

        digits.clear();
        var b = begin;
        var e = end;
        begin = null;
        end = null;

        return b != null && e != null && res.isPresent()
            ? Optional.of(new ParsedToken<>(res.get(), b, e))
            : Optional.empty();
    }
}
