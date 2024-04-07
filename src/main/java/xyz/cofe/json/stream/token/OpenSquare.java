package xyz.cofe.json.stream.token;

/**
 * Открытая квадратная скобка
 */
public record OpenSquare<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
}
