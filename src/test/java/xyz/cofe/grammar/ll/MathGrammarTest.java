package xyz.cofe.grammar.ll;

import org.junit.jupiter.api.Test;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.grammar.ll.lexer.Lexer;
import xyz.cofe.grammar.ll.parser.AstParser;
import xyz.cofe.grammar.ll.parser.Param;
import xyz.cofe.grammar.ll.parser.SomeParsers;

import java.text.DecimalFormat;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MathGrammarTest {
    @Test
    public void intParse(){
        var res1 = IntNumber.parse("123",0);
        assertTrue(res1.isPresent());
        System.out.println(res1);

        var res2 = IntNumber.parse("123 ",0);
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
    public void parse1(){
        var rawParser = SomeParsers.parse(MathGrammar.class);

        var lexer = Lexer.build(MathGrammar.class);
        var astParser = rawParser.validate(lexer).map(ruleParsers -> new AstParser(ruleParsers, lexer));

        astParser.getError().ifPresent(System.err::println);

        var parser = astParser.getOk().get();
        var source = "1 + 2 * 3 + 4";
        var tokens = lexer.parse(source,0);

        var ptr = new Pointer.ImListPointer<>( tokens );

        var parsedOpt = parser.parse(Expr.class, ptr);
        var parsed = parsedOpt.get().value();
        System.out.println(parsed);
    }

    @Test
    public void parse2(){
        var rawParser = SomeParsers.parse(MathGrammar.class);

        var lexer = Lexer.build(MathGrammar.class);
        var astParser = rawParser.validate(lexer).map(ruleParsers -> new AstParser(ruleParsers, lexer));

        astParser.getError().ifPresent(System.err::println);

        var parser = astParser.getOk().get();
        var source = "a = 1 + b";
        var tokens = lexer.parse(source,0);

        var ptr = new Pointer.ImListPointer<>( tokens );

        var parsedOpt = parser.parse(Expr.class, ptr);
        var parsed = parsedOpt.get().value();
        System.out.println(parsed);
    }
}
