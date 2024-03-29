package xyz.cofe.grammar.ll.lexer;

public class ParserImplError extends Error {
    private final TokenParser parser;
    private final String source;
    private final int offset;
    private final Matched<?> matched;

    public ParserImplError(TokenParser parser, String source, int offset, Matched<?> matched) {
        super("token parser implementation broken");
        this.parser = parser;
        this.source = source;
        this.offset = offset;
        this.matched = matched;
    }

    public TokenParser getParser() {
        return parser;
    }

    public String getSource() {
        return source;
    }

    public int getOffset() {
        return offset;
    }

    public Matched<?> getMatched() {
        return matched;
    }
}
