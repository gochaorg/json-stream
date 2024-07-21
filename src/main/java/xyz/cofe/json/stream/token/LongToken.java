package xyz.cofe.json.stream.token;

/**
 * Лексема целого числа (64 бит)
 * @param value значение
 * @param begin начало лексемы
 * @param end конец лексемы
 * @param <S> тип исходника
 */
public record LongToken<S extends CharPointer<S>>(
    long value,
    S begin,
    S end
) implements Token<S> {
}
