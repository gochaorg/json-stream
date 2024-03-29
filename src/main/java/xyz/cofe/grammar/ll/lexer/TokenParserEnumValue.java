package xyz.cofe.grammar.ll.lexer;

import xyz.cofe.coll.im.Tuple2;
import xyz.cofe.grammar.ll.bind.TermBind;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record TokenParserEnumValue(TermBind[] binds,
                                   Type tokenType,
                                   Object enumValue,
                                   String[] expectedText
                                   ) implements TokenParser
{
    private static String[] computeTerms(TermBind[] binds){
        List<Tuple2<String,TermBind>> terms = new ArrayList<>();
        for(var b : binds){
            terms.add( Tuple2.of(b.value(), b) );
        }

        terms.sort( (a,b) -> {
            var lenCmp = Integer.compare(a._1().length(), b._1().length());
            if( lenCmp!=0 )return -lenCmp;

            return a._2().value().compareTo(b._2().value());
        });

        return terms.stream().map(x -> x._2().value()).toList().toArray(new String[0]);
    }

    public TokenParserEnumValue(TermBind[] binds,
                                Type tokenType,
                                Object enumValue
    ) {
        this(binds, tokenType, enumValue, computeTerms(binds));
    }

    @Override
    public Optional<Matched<?>> parse(String source, int offset) {
        if (source == null) throw new IllegalArgumentException("source==null");
        if (offset < 0) throw new IllegalArgumentException("offset<0");
        if (offset >= source.length()) throw new IllegalArgumentException("offset>=source.length()");

        for( String expected : expectedText ){
            if( source.startsWith(expected,offset) ){
                return Optional.of(new Matched<>(enumValue, source, offset, offset+expected.length()));
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean hasSkip() {
        boolean skip = false;
        for (var b : binds) {
            skip = skip || b.skip();
        }
        return skip;
    }
}
