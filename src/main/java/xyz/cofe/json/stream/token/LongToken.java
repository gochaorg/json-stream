package xyz.cofe.json.stream.token;

public record LongToken<S extends CharPointer<S>>(
    long value,
    S begin,
    S end
) implements Token<S> {
}
