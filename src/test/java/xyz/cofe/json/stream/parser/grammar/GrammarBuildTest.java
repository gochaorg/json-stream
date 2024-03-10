package xyz.cofe.json.stream.parser.grammar;

import org.junit.jupiter.api.Test;
import xyz.cofe.coll.im.HTree;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.htree.Nest;
import xyz.cofe.json.stream.parser.grammar.impl.Ascii;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GrammarBuildTest {
    @Test
    public void describe() {
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

        assertTrue(gr.rules().size() == 4);

        gr.rules().each(rule -> {
            System.out.println("rule " +
                Ascii.bold + rule.name() + Ascii.reset
            );
            rule.definition().walk().tree().each(defpath -> {
                String ident = ">>> ".repeat(defpath.directPath().size());
                String nodeText = switch (defpath.definition()) {
                    case Grammar.Term(var txt) -> "Term " + txt;
                    case Grammar.Ref(var ref) -> "Ref " + Ascii.italicOn + ref + Ascii.reset;
                    case Grammar.Repeat r -> "Repeat";
                    case Grammar.Alternative a -> "Alternative";
                    case Grammar.Sequence s -> "Sequence";
                };
                System.out.println(
                    Ascii.Color.White.foreground() +
                        ident +
                        Ascii.Color.Default.foreground() +
                        nodeText
                );
            });
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
