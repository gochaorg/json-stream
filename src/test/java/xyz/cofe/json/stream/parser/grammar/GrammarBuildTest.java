package xyz.cofe.json.stream.parser.grammar;

import org.junit.jupiter.api.Test;
import xyz.cofe.coll.im.HTree;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.htree.Nest;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GrammarBuildTest {
    @Test
    public void test1() {
        var gr =
            Grammar.grammar()
                .rule("exp", exp -> {
                    exp.ref("sum");
                })
                .rule("sum", sum -> {
                    sum.ref("mul").term("+").ref("exp")
                        .alt()
                        .ref("mul");
                })
                .rule("mul", mul -> {
                    mul.ref("atom").term("*").ref("exp")
                        .alt()
                        .ref("atom");
                })
                .rule("atom", atom -> {
                    atom.term("1")
                        .alt()
                        .term("(").ref("exp").term(")");
                })
                .build();

        HTree.visit(gr, new Object() {
            void enter(ImList<Nest.PathNode> path) {
                System.out.println("  ".repeat(path.size()) + " " + path.head().get().pathValue());
            }
        });


        System.out.println("---------");
        gr.rules().get(0).ifPresent(r -> {
            System.out.println("rule " + r.name());
            r.definition().walk().go().each(System.out::println);
        });
    }

    @Test
    public void duplicate() {
        var gr =
            Grammar.grammar()
                .rule("exp", exp -> {
                    exp.ref("sum");
                })
                .rule("sum", sum -> {
                    sum.ref("mul").term("+").ref("exp")
                        .alt()
                        .ref("mul");
                })
                .rule("sum", mul -> {
                    mul.ref("atom").term("*").ref("exp")
                        .alt()
                        .ref("atom");
                })
                .rule("atom", atom -> {
                    atom.term("1")
                        .alt()
                        .term("(").ref("exp").term(")");
                })
                .build();

        var dup = DuplicateRuleName.find(gr);
        dup.each(System.out::println);

        assertTrue(dup.size() > 1);
    }

    @Test
    public void brokenRef() {
        var gr =
            Grammar.grammar()
                .rule("exp", exp -> {
                    exp.ref("sum");
                })
                .rule("sum", sum -> {
                    sum.ref("mul").term("+").ref("expr")
                        .alt()
                        .ref("mul");
                })
                .rule("mul", mul -> {
                    mul.ref("atom").term("*").ref("exp")
                        .alt()
                        .ref("atom");
                })
                .rule("atom", atom -> {
                    atom.term("1")
                        .alt()
                        .term("(").ref("exp").term(")");
                })
                .build();

        var br = BrokenRef.find(gr);
        br.each(System.out::println);

        assertTrue(br.size() > 0);
    }

    @Test
    public void recusiveFind() {
        var gr =
            Grammar.grammar()
                .rule("exp", exp -> {
                    exp.ref("sum");
                })
                .rule("sum", sum -> {
                    sum.ref("mul").term("+").ref("exp")
                        .alt()
                        .ref("mul");
                })
                .rule("mul", mul -> {
                    mul.ref("atom").term("*").ref("exp")
                        .alt()
                        .ref("atom");
                })
                .rule("atom", atom -> {
                    atom.term("1")
                        .alt()
                        .term("(").ref("exp").term(")");
                })
                .build();

        var recursiveRefs = RecursiveRef.find(gr);
        recursiveRefs.each(rr -> {
            var ruleName = rr.revPath().head().map(n -> n.ref().name()).orElse("?");

            var path = rr.revPath().map(
                    n -> n.rule().name() + "/" + n.ref().name() + "[" + n.rule().indexOf(n.ref()) + "]"
                ).reverse()
                .foldLeft("", (acc, it) -> acc.isBlank() ? it : acc + " > " + it);

            System.out.println("recursive rule " + ruleName + ", revPath " + path);
        });
    }
}
