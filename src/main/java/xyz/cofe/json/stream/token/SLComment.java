package xyz.cofe.json.stream.token;

import java.util.Optional;

/**
 * Однострочный комментарий
 * @param value текст комментария
 * @param begin начало в исходнике
 * @param end конец в исходнике
 * @param <S> Тип исходника
 */
public record SLComment<S extends CharPointer<S>>(
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
            if( ptr==null ) throw new IllegalArgumentException("ptr==null");
            var begin = ptr;
            var buff = new StringBuilder();

            if( !ptr.get(0).map(c -> c=='/').orElse(false) )return Optional.empty();
            if( !ptr.get(1).map(c -> c=='/').orElse(false) )return Optional.empty();

            buff.append("//");

            ptr = ptr.move(2);
            while (true){
                if( ptr.get(0).isEmpty() )break;
                if( ptr.get(0).map(c -> c=='\r').orElse(false) && ptr.get(1).map(c -> c=='\n').orElse(false) ){
                    ptr = ptr.move(2);
                    break;
                }
                if( ptr.get(0).map(c -> c=='\n').orElse(false) ){
                    ptr = ptr.move(1);
                    break;
                }
                buff.append(ptr.get(0).get());
                ptr = ptr.move(1);
            }

            return Optional.of( new SLComment<>(buff.toString(), begin, ptr) );
        }
    }
}
