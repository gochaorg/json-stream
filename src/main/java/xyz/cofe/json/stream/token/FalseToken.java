package xyz.cofe.json.stream.token;

public record FalseToken<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
}
