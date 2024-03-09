package xyz.cofe.json.stream.parser.grammar;

import org.junit.jupiter.api.Test;

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

        var lrec = LeftRecursion.find(gr);

        System.out.println("found " + lrec.size() + ":");
        lrec.each(rec -> System.out.println(rec.leftRecursion()));

        assertTrue(lrec.size() > 0);
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
                    sum.ref("a").ref("c");
                })
                .rule("c", sum -> {
                    sum.term("d").ref("c");
                })
                .build();

        var lrec = LeftRecursion.find(gr);

        System.out.println("found " + lrec.size() + ":");
        lrec.each(rec -> System.out.println(rec.leftRecursion()));

        assertTrue(lrec.size() > 0);
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

        System.out.println(gr);

        var recur = RecursiveRef.find(gr);
        System.out.println("rec found " + recur.size() + ":");
        recur.each(System.out::println);

        var lrec = LeftRecursion.find(gr);

        System.out.println("left found " + lrec.size() + ":");
        lrec.each(rec -> System.out.println(rec.leftRecursion()));
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

        var lrec = LeftRecursion.find(gr);

        System.out.println("found " + lrec.size() + ":");
        lrec.each(rec -> System.out.println(rec.leftRecursion()));
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

        var lrec = LeftRecursion.find(gr);

        System.out.println("found " + lrec.size() + ":");
        lrec.each(rec -> System.out.println(rec.leftRecursion()));
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

        var lrec = LeftRecursion.find(gr);

        System.out.println("found " + lrec.size() + ":");
        lrec.each(rec -> System.out.println(rec.leftRecursion()));
    }
}
