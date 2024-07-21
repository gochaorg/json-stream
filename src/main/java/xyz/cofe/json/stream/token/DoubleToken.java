package xyz.cofe.json.stream.token;

/**
 * Плавающее число
 * @param value число
 * @param begin начало лексемы
 * @param end конец лексемы
 * @param <S>
 */
public record DoubleToken<S extends CharPointer<S>>(
    double value,
    S begin,
    S end
) implements Token<S> {
}
