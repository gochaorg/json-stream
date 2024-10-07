package xyz.cofe.json.stream.token;

/**
 * Закрытая фигурная скобка
 * @param begin начало лексемы
 * @param end конец лексемы
 * @param <S> Указатель на символы строки
 */
public record CloseParentheses<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
}
