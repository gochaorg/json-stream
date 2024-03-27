package xyz.cofe.json.stream.parser.grammar;

import org.junit.jupiter.api.Test;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.parser.grammar.lexer.Lexer;
import xyz.cofe.json.stream.parser.grammar.parser.AstParser;
import xyz.cofe.json.stream.parser.grammar.parser.Param;
import xyz.cofe.json.stream.parser.grammar.parser.SomeParsers;

import java.text.DecimalFormat;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MathGrammarTest {
    @Test
    public void intParse(){
        var res1 = MathGrammar.IntNumber.parse("123",0);
        assertTrue(res1.isPresent());
        System.out.println(res1);

        var res2 = MathGrammar.IntNumber.parse("123 ",0);
        assertTrue(res2.isPresent());
        System.out.println(res2);
    }

    @Test
    public void lexTest(){
        var lexer = Lexer.build(MathGrammar.class);
        var tokens = lexer.parse("12 + 3 * ( 4 - 8 ) / 2",0);

        var df = new DecimalFormat("#000");
        for( var t : tokens ){
            System.out.println("["+
                df.format(t.begin())+
                " "+
                df.format(t.end())+
                "] "+
                t.result()
            );
        }
    }

    @Test
    public void parseTest(){
        //var parser = Parser.build(MathGrammar.class);
        var parser1 = SomeParsers.parse(MathGrammar.class);
        var someParsers = parser1.parsersOf(MathGrammar.Atom.class);
        System.out.println("someParsers count "+someParsers.size());

        var lexer = Lexer.build(MathGrammar.class);
        var parser2 = parser1.validate(lexer).map(AstParser::new);

        System.out.println(parser2.isOk());

        var parser = parser2.getOk().get();

        Class<?> tok = MathGrammar.Atom.class;

        var thisParsers = parser.parsersOf(tok);
        thisParsers.enumerate().each( itParser -> {
            System.out.println("("+itParser.index()+") input ");
            itParser.value().inputPattern().enumerate().each(param -> {
                System.out.println("  " + param.value().node());
                var lines = switch (param.value()){
                    case Param.RuleRef rr ->
                        (ImList<String>) rr.parsers().map(m -> m.method().toString() );

                    case Param.TermRef tr ->
                        (ImList<String>) tr.parsers().map( t -> {
                            return t.tokenType().toString() + " bind=" + Arrays.toString(t.binds()) + " "+itParser.value().termBindsOfParam((int)param.index());
                        });
                };
                lines.each( line -> {
                    System.out.println("    "+line);
                });
            });
            System.out.println("  out "+itParser.value().returnType());
        });

        var source = "12 + 3";
        var ptr = new Pointer.ImListPointer<>( lexer.parse(source,0) );

        var parsedOpt = parser.parse(MathGrammar.PlusOperation.class, ptr);
        System.out.println(parsedOpt.get());

        var parsed = parsedOpt.get().value();
    }
}
