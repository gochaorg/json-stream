package xyz.cofe.json.stream.token;

/**
 * Null лексема
 * @param begin начало в исходнике
 * @param end конец в исходнике
 * @param <S> тип исходника
 */
public record NullToken<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
}
