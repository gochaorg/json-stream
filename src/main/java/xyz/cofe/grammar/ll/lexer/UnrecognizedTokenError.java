package xyz.cofe.grammar.ll.lexer;

import xyz.cofe.coll.im.ImList;

public class UnrecognizedTokenError extends Error {
    private static String unparsedPart(String source, int offset) {
        if (source == null) return "";
        if (offset < 0) return "";
        if (offset >= source.length()) return "";

        String tail = source.substring(offset);
        if (tail.length() > 50) return tail.substring(0, 50);

        return tail;
    }

    public UnrecognizedTokenError(ImList<Matched<?>> parsed, String source, int offset) {
        super("can't parse source text, offset=" + offset + "\nsource part=" + unparsedPart(source, offset));
        this.parsed = parsed;
        this.source = source;
        this.offset = offset;
    }

    private final ImList<Matched<?>> parsed;
    private final String source;
    private final int offset;

    public ImList<Matched<?>> getParsed() {
        return parsed;
    }

    public String getSource() {
        return source;
    }

    public int getOffset() {
        return offset;
    }
}
