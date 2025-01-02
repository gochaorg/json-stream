package xyz.cofe.json.stream.token;

import xyz.cofe.coll.im.ImList;

import java.util.Optional;

/**
 * Парсинг лексем
 * @param <S> расположение в исходнике
 */
public class Tokenizer<S extends CharPointer<S>> {
    private final ImList<TokenParser<S>> parsers;

    /**
     * Конструктор
     * @param parsers парсеры
     */
    public Tokenizer(Iterable<TokenParser<S>> parsers) {
        if (parsers == null) throw new IllegalArgumentException("parsers==null");
        this.parsers = ImList.from(parsers);
    }

    /**
     * Конструктор
     * @param parsers парсеры
     */
    @SafeVarargs
    public Tokenizer(TokenParser<S>... parsers) {
        if (parsers == null) throw new IllegalArgumentException("parsers==null");
        this.parsers = ImList.of(parsers);
    }

    /**
     * Распознанные лексемы
     * @param tokens лексемы
     * @param next конец последней лексемы
     * @param <S> тип исходника
     */
    public record Parsed<S extends CharPointer<S>>(ImList<Token<S>> tokens, S next) {}

    public Parsed<S> parse(S ptr) {
        if (ptr == null) throw new IllegalArgumentException("ptr==null");
        ImList<Token<S>> tokens = ImList.of();

        while (true) {
            Optional<? extends Token<S>> tokOpt = Optional.empty();
            for (var parser : parsers) {
                tokOpt = parser.parse(ptr);
                if( tokOpt.isPresent() ){
                    break;
                }
            }

            if( tokOpt.isPresent() ) {
                Token<S> tokTup = tokOpt.get();
                tokens = tokens.prepend(tokTup);
                ptr = tokTup.end();
            }else{
                break;
            }
        }

        return new Parsed<>(tokens.reverse(), ptr);
    }

    /**
     * Парсинг
     * @param source исходник
     * @return лексемы
     */
    public static Parsed<StringPointer> parse(String source){
        if( source==null ) throw new IllegalArgumentException("source==null");
        var ptr = new StringPointer(source,0);
        Tokenizer<StringPointer> tokenizer = defaultTokenizer();
        return tokenizer.parse(ptr);
    }

    /**
     * Парсер по умолчанию
     * @return Лексический анализатор
     * @param <S> тип исходника
     */
    public static <S extends CharPointer<S>> Tokenizer<S> defaultTokenizer(){
        return new Tokenizer<S>(
            new KeyWordParser<>(),
            new NumberParser<>(),
            new StringToken.Parser<>(),
            new Whitespace.Parser<>(),
            new IdentifierToken.Parser<>(),
            new SLComment.Parser<>(),
            new MLComment.Parser<>()
        );
    }
}
