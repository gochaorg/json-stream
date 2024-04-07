package xyz.cofe.json.stream.token;

/**
 * Закрытая квадратная скобка
 */
public record CloseSquare<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
}
