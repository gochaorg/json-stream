package xyz.cofe.json.stream.token;

import java.util.Optional;

/**
 * Парсер лексемы
 * @param <S> Указатель на символы строки
 */
public interface TokenParser<S extends CharPointer<S>> {
    /**
     * Парсинг лексемы
     * @param ptr исходник
     * @return лексема
     */
    public Optional<? extends Token<S>> parse(S ptr);
}
