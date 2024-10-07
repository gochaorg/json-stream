package xyz.cofe.json.stream.token;

/**
 * Открытая квадратная скобка
 * @param begin начало лексемы
 * @param end конец лексемы
 * @param <S> Указатель на символы строки
 */
public record OpenSquare<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
}
