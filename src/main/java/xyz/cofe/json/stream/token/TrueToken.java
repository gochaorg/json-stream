package xyz.cofe.json.stream.token;

/**
 * Лексема true
 * @param begin начало в исходнике
 * @param end конец в исходнике
 * @param <S> Тип исходника
 */
public record TrueToken<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
}
