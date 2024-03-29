package xyz.cofe.grammar;

import org.junit.jupiter.api.Test;
import xyz.cofe.grammar.Grammar;
import xyz.cofe.grammar.RecursiveRef;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LeftRecurTest {
    /**
     * Простая левая рекурсия
     * <pre>
     * A ::= A
     * </pre>
     */
    @Test
    public void simpleRecur() {
        var gr =
            Grammar.grammar()
                .rule("a", exp -> {
                    exp.ref("a");
                })
                .rule("b", sum -> {
                    sum.ref("c").ref("b");
                })
                .build();

        var rrefs = RecursiveRef.find(gr);
        System.out.println("recursions:");
        rrefs.each(System.out::println);

        var lrefCount = rrefs.foldLeft(0,(acc,it)->acc + (it.isLeftRecursion() ? 1 : 0));
        System.out.println("lrefCount "+lrefCount);

        assertTrue(lrefCount>0);
    }

    /**
     * Простая левая рекурсия
     * <pre>
     * A ::= B
     * B ::= A
     * </pre>
     */
    @Test
    public void recurChainSimple() {
        var gr =
            Grammar.grammar()
                .rule("a", exp -> {
                    exp.ref("b");
                })
                .rule("b", sum -> {
                    sum.ref("a");
                })
                .build();

        var rrefs = RecursiveRef.find(gr);
        System.out.println("recursions:");
        rrefs.each(System.out::println);

        var lrefCount = rrefs.foldLeft(0,(acc,it)->acc + (it.isLeftRecursion() ? 1 : 0));
        System.out.println("lrefCount "+lrefCount);

        assertTrue(lrefCount>0);
    }

    /**
     * <pre>
     * A ::= B | A
     * B ::= t
     * </pre>
     */
    @Test
    public void altCase() {
        var gr =
            Grammar.grammar()
                .rule("a", exp -> {
                    exp
                        .ref("b")
                        .alt()
                        .ref("a");
                })
                .rule("b", sum -> {
                    sum.term("t");
                })
                .build();

        var rrefs = RecursiveRef.find(gr);
        System.out.println("recursions:");
        rrefs.each(System.out::println);

        var lrefCount = rrefs.foldLeft(0,(acc,it)->acc + (it.isLeftRecursion() ? 1 : 0));
        System.out.println("lrefCount "+lrefCount);

        assertTrue(lrefCount>0);
    }

    /**
     * <pre>
     * A ::= { B } A
     * B ::= t
     * </pre>
     */
    @Test
    public void repeatFirst() {
        var gr =
            Grammar.grammar()
                .rule("a", exp -> {
                    exp
                        .ref("b")
                        .alt()
                        .ref("a");
                })
                .rule("b", sum -> {
                    sum.ref("a").ref("b");
                })
                .build();


        var rrefs = RecursiveRef.find(gr);
        System.out.println("recursions:");
        rrefs.each(System.out::println);

        var lrefCount = rrefs.foldLeft(0,(acc,it)->acc + (it.isLeftRecursion() ? 1 : 0));
        System.out.println("lrefCount "+lrefCount);

        assertTrue(lrefCount>0);
    }

    /**
     * <pre>
     * A ::= { A } B
     * B ::= t B
     * </pre>
     */
    @Test
    public void repeatInside() {
        var gr =
            Grammar.grammar()
                .rule("a", exp -> {
                    exp
                        .repeat( r -> r.ref("a"))
                        .ref("b");
                })
                .rule("b", sum -> {
                    sum.term("t").ref("b");
                })
                .build();


        var rrefs = RecursiveRef.find(gr);
        System.out.println("recursions:");
        rrefs.each(System.out::println);

        var lrefCount = rrefs.foldLeft(0,(acc,it)->acc + (it.isLeftRecursion() ? 1 : 0));
        System.out.println("lrefCount "+lrefCount);

        assertTrue(lrefCount>0);
    }

    /**
     * <pre>
     * A ::= { B } { A }
     * B ::= t B
     * </pre>
     */
    @Test
    public void repeatInsideTwice() {
        var gr =
            Grammar.grammar()
                .rule("a", exp -> {
                    exp
                        .repeat( r -> r.ref("b"))
                        .repeat( r -> r.ref("a"))
                        ;
                })
                .rule("b", sum -> {
                    sum.term("t").ref("b");
                })
                .build();


        var rrefs = RecursiveRef.find(gr);
        System.out.println("recursions:");
        rrefs.each(System.out::println);

        var lrefCount = rrefs.foldLeft(0,(acc,it)->acc + (it.isLeftRecursion() ? 1 : 0));
        System.out.println("lrefCount "+lrefCount);

        assertTrue(lrefCount>0);
    }
}
