package xyz.cofe.json.stream.token;

import java.util.Optional;
import java.util.function.Function;

/**
 * Парсер ключевых слов:
 * {@link NullToken}, {@link TrueToken}, {@link FalseToken},
 * {@link OpenParentheses}, {@link CloseParentheses},
 * {@link OpenSquare}, {@link CloseSquare},
 * {@link Comma}, {@link Colon}
 * @param <S> Тип исходника
 */
public class KeyWordParser<S extends CharPointer<S>> implements TokenParser<S> {
    private Function<S,Optional<? extends Token<S>>> nullKeyword = ptr -> TokenParsers.expect(ptr,"null",false).map(r -> new NullToken<>(ptr, r._2()));
    private Function<S,Optional<? extends Token<S>>> falseKeyword = ptr -> TokenParsers.expect(ptr,"false",false).map(r -> new FalseToken<>(ptr, r._2()));
    private Function<S,Optional<? extends Token<S>>> trueKeyword = ptr -> TokenParsers.expect(ptr,"true",false).map(r -> new TrueToken<>(ptr, r._2()));
    private Function<S,Optional<? extends Token<S>>> openParentheses = ptr -> TokenParsers.expect(ptr,"{",false).map(r -> new OpenParentheses<>(ptr, r._2()));
    private Function<S,Optional<? extends Token<S>>> closeParentheses = ptr -> TokenParsers.expect(ptr,"}",false).map(r -> new CloseParentheses<>(ptr, r._2()));
    private Function<S,Optional<? extends Token<S>>> openSquare = ptr -> TokenParsers.expect(ptr,"[",false).map(r -> new OpenSquare<>(ptr, r._2()));
    private Function<S,Optional<? extends Token<S>>> closeSquare = ptr -> TokenParsers.expect(ptr,"]",false).map(r -> new CloseSquare<>(ptr, r._2()));
    private Function<S,Optional<? extends Token<S>>> comma = ptr -> TokenParsers.expect(ptr,",",false).map(r -> new Comma<>(ptr, r._2()));
    private Function<S,Optional<? extends Token<S>>> colon = ptr -> TokenParsers.expect(ptr,":",false).map(r -> new Colon<>(ptr, r._2()));

    @Override
    public Optional<? extends Token<S>> parse(S ptr) {
        if( ptr==null ) throw new IllegalArgumentException("ptr==null");

        var r = nullKeyword.apply(ptr);
        if( r.isPresent())return r;

        r = falseKeyword.apply(ptr);
        if( r.isPresent())return r;

        r = trueKeyword.apply(ptr);
        if( r.isPresent())return r;

        r = openParentheses.apply(ptr);
        if( r.isPresent())return r;

        r = closeParentheses.apply(ptr);
        if( r.isPresent())return r;

        r = openSquare.apply(ptr);
        if( r.isPresent())return r;

        r = closeSquare.apply(ptr);
        if( r.isPresent())return r;

        r = comma.apply(ptr);
        if( r.isPresent())return r;

        r = colon.apply(ptr);
        return r;
    }
}
