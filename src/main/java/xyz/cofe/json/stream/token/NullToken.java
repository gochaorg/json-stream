package xyz.cofe.json.stream.token;

public record NullToken<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
}
