package xyz.cofe.grammar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class FirstTest {

    @Test
    public void firstAtom(){
        var gr = GrammarBuildTest.validMath;

        var rule = gr.firstRule("atom").get();
        var first = First.find(gr, rule);
        first.each(System.out::println);

        assertTrue( first.size()==2 );
        assertTrue( first.find(f -> f.term().text().equals("1")).isPresent() );
        assertTrue( first.find(f -> f.term().text().equals("(")).isPresent() );
    }

    @Test
    public void firstMul(){
        var gr = GrammarBuildTest.validMath;

        var rule = gr.firstRule("mul").get();
        var first = First.find(gr, rule);
        first.each(System.out::println);

        assertTrue( first.size()==2 );
        assertTrue( first.find(f -> f.term().text().equals("1")).isPresent() );
        assertTrue( first.find(f -> f.term().text().equals("(")).isPresent() );
    }

    @Test
    public void firstSum(){
        var gr = GrammarBuildTest.validMath;

        var rule = gr.firstRule("sum").get();
        var first = First.find(gr, rule);
        first.each(System.out::println);

        assertTrue( first.size()==2 );
        assertTrue( first.find(f -> f.term().text().equals("1")).isPresent() );
        assertTrue( first.find(f -> f.term().text().equals("(")).isPresent() );
    }

    @Test
    public void firstExp(){
        var gr = GrammarBuildTest.validMath;

        var rule = gr.firstRule("exp").get();
        var first = First.find(gr, rule);
        first.each(System.out::println);

        assertTrue( first.size()==2 );
        assertTrue( first.find(f -> f.term().text().equals("1")).isPresent() );
        assertTrue( first.find(f -> f.term().text().equals("(")).isPresent() );
    }
}
