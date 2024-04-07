package xyz.cofe.grammar.lr;

import java.util.Optional;

public class StringSource implements SourcePointer<Character, StringSource> {
    public final String source;
    public final int offset;

    public StringSource(String source, int offset) {
        if (source == null) throw new IllegalArgumentException("source==null");

        this.source = source;
        this.offset = offset;
    }

    public boolean eof(){ return offset>=source.length(); }

    @Override
    public StringSource move(int offset) {
        return new StringSource(source, this.offset + offset);
    }

    @Override
    public Optional<Character> get() {
        if (offset < 0 || offset >= source.length()) return Optional.empty();
        var chr = source.charAt(offset);
        return Optional.of(chr);
    }

    @Override
    public int substract(StringSource other) {
        if( other==null ) throw new IllegalArgumentException("other==null");
        return offset - other.offset;
    }
}
