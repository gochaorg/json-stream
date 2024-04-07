package xyz.cofe.json.stream.token;

public record IntToken<S extends CharPointer<S>>(
    int value,
    S begin,
    S end
) implements Token<S> {
}
