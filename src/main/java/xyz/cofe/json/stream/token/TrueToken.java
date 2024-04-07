package xyz.cofe.json.stream.token;

public record TrueToken<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
}
