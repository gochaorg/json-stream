package xyz.cofe.json.stream.token;

/**
 * Лексема целого числа (32 бит)
 * @param value значение
 * @param begin начало лексемы
 * @param end конец лексемы
 * @param <S> тип исходника
 */
public record IntToken<S extends CharPointer<S>>(
    int value,
    S begin,
    S end
) implements Token<S> {
}
