package xyz.cofe.grammar.lr;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Tokenizer {
    private final List<TokenParser<?,StringSource>> parsers;

    public Tokenizer(Iterable<TokenParser<?,StringSource>> parsers){
        if( parsers==null ) throw new IllegalArgumentException("parsers==null");
        this.parsers = new ArrayList<>();
        for( var p : parsers )this.parsers.add(p);
    }

    public Tokenizer(TokenParser<?,StringSource> ... parsers){
        if( parsers==null ) throw new IllegalArgumentException("parsers==null");
        this.parsers = new ArrayList<>();
        this.parsers.addAll(Arrays.asList(parsers));
    }

    public void reset(){
        this.parsers.forEach(TokenParser::reset);
    }

    public ImList<ParsedToken<?,StringSource>> parse(String source){
        if( source==null ) throw new IllegalArgumentException("source==null");
        List<ParsedToken<?,StringSource>> result = new ArrayList<>();

        StringSource src = new StringSource(source,0);
        List<ParsedToken<?,StringSource>> intermediate = new ArrayList<>();

        while (!src.eof()){
            intermediate.clear();

            for( var parser : parsers ){
                parser.input(src).ifPresent(intermediate::add);
            }
            src = src.move(1);

            if( !intermediate.isEmpty() ){
                if( intermediate.size()==1 ){
                    result.addAll(intermediate);
                }else{
                    intermediate.sort( (a,b) -> a.begin().substract(b.begin()) );
                    result.addAll(intermediate);
                }
            }
        }

        // check overlap or hole
        var overlapHoles = ImList.from(intermediate).zip(ImList.from(intermediate).skip(1))
            .foldLeft(
                Tuple2.of(0,0),
                (acc,it) -> {
                    int endBeginMatch = it._1().end().substract(it._2().begin());
                    return Tuple2.of(
                        acc._1() + (Math.min(endBeginMatch, 0)),
                        acc._2() + (Math.max(endBeginMatch, 0))
                    );
                }
            );

        var overlap = overlapHoles._1();
        var holes = overlapHoles._2();
        if( overlap<0 ) System.err.println("overlap "+ Math.abs(overlap));
        if( holes<0 ) System.err.println("holes "+ Math.abs(holes));

        return ImList.from(result);
    }
}
