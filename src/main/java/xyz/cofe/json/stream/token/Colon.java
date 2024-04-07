package xyz.cofe.json.stream.token;

/**
 * двоеточие
 */
public record Colon<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
}
