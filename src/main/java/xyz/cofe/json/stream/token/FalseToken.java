package xyz.cofe.json.stream.token;

/**
 * Лексема False
 * @param begin начало лексемы
 * @param end конец лексемы
 * @param <S> тип исходника
 */
public record FalseToken<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
}
