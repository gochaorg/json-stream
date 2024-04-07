package xyz.cofe.json.stream.token;

import xyz.cofe.coll.im.ImList;

import java.util.Optional;

public class Tokenizer<S extends CharPointer<S>> {
    private final ImList<TokenParser<S>> parsers;

    public Tokenizer(Iterable<TokenParser<S>> parsers) {
        if (parsers == null) throw new IllegalArgumentException("parsers==null");
        this.parsers = ImList.of(parsers);
    }

    @SafeVarargs
    public Tokenizer(TokenParser<S>... parsers) {
        if (parsers == null) throw new IllegalArgumentException("parsers==null");
        this.parsers = ImList.of(parsers);
    }

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

    public static Parsed<StringPointer> parse(String source){
        if( source==null ) throw new IllegalArgumentException("source==null");
        var ptr = new StringPointer(source,0);
        Tokenizer<StringPointer> tokenizer = defaultTokenizer();
        return tokenizer.parse(ptr);
    }

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
