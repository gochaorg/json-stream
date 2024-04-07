package xyz.cofe.json.stream.token;

/**
 * Открытая фигурная скобка
 */
public record OpenParentheses<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
}
