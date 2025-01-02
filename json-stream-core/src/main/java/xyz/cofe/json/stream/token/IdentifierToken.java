package xyz.cofe.json.stream.token;

import java.util.Optional;

/**
 * Лексема идентификатор
 * @param value значение лексемы
 * @param begin начало лексемы
 * @param end конец лексемы
 * @param <S> тип исходника
 */
public record IdentifierToken<S extends CharPointer<S>>(
    String value,
    S begin,
    S end
) implements Token<S> {
    /**
     * Парсер лексемы
     * @param <S> Тип исходника
     */
    public static class Parser<S extends CharPointer<S>> implements TokenParser<S> {
        @Override
        public Optional<? extends Token<S>> parse(S ptr) {
            if (ptr == null) throw new IllegalArgumentException("ptr==null");

            var begin = ptr;

            var first = ptr.get(0).flatMap(c -> Character.isLetter(c) || c == '_' || c == '$' ? Optional.of(c) : Optional.empty());
            if (first.isEmpty()) return Optional.empty();

            StringBuilder buff = new StringBuilder();
            buff.append(first.get());

            ptr = ptr.move(1);
            while (true){
                var secord = ptr.get(0).flatMap(c -> Character.isLetter(c) || c == '_' || c == '$' ? Optional.of(c) : Optional.empty());
                if( secord.isEmpty() )break;
                buff.append(secord.get());
                ptr = ptr.move(1);
            }

            return Optional.of(new IdentifierToken<>(buff.toString(), begin, ptr));
        }
    }
}
