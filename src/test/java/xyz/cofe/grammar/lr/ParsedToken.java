package xyz.cofe.grammar.lr;

public record ParsedToken<R, S extends SourcePointer<?, ?>>(R token, S begin, S end) {}
