package xyz.cofe.grammar.lr;

import java.util.Optional;

public class Whitespace implements TokenParser<String,StringSource> {
    private final StringBuilder buffer = new StringBuilder();
    private StringSource begin;
    private StringSource end;

    @Override
    public void reset() {
        begin = null;
        end = null;
        buffer.setLength(0);
    }

    @Override
    public boolean hasState() {
        return !buffer.isEmpty();
    }

    public Optional<ParsedToken<String, StringSource>> input(StringSource src) {
        if (src == null) throw new IllegalArgumentException("src==null");

        var chrOpt = src.get();
        if (chrOpt.isEmpty()) {
            return flush();
        }

        var chr = chrOpt.get();
        if (!Character.isWhitespace(chr)) {
            return flush();
        }

        if (begin == null) begin = src;
        end = src;
        buffer.append(chr);

        return Optional.empty();
    }

    private Optional<ParsedToken<String, StringSource>> flush() {
        if (begin != null && end != null && !buffer.isEmpty()) {
            var b = begin;
            var e = end;
            var r = buffer.toString();
            buffer.setLength(0);
            begin = null;
            end = null;
            return Optional.of(
                new ParsedToken<>(r, b, e)
            );
        }
        return Optional.empty();
    }
}
