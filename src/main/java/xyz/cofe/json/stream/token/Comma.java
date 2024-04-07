package xyz.cofe.json.stream.token;

/**
 * запятая
 */
public record Comma<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
}
