package xyz.cofe.json.stream.token;

/**
 * Закрытая фигурная скобка
 */
public record CloseParentheses<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
}
