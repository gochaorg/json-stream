package xyz.cofe.json.stream.token;

public record DoubleToken<S extends CharPointer<S>>(
    double value,
    S begin,
    S end
) implements Token<S> {
}
