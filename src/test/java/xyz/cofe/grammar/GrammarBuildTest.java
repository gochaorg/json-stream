package xyz.cofe.grammar;

import org.junit.jupiter.api.Test;
import xyz.cofe.grammar.BrokenRef;
import xyz.cofe.grammar.DuplicateRuleName;
import xyz.cofe.grammar.Grammar;
import xyz.cofe.grammar.RecursiveRef;
import xyz.cofe.grammar.impl.Ascii;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GrammarBuildTest {
    public static Grammar validMath =
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

    @Test
    public void describe() {
        var gr = validMath;

        assertTrue(gr.rules().size() == 4);

        gr.rules().each(rule -> {
            System.out.println("rule " +
                Ascii.Color.Red.foreground() + Ascii.bold + rule.name() + Ascii.reset
            );
            rule.definition().walk().tree().each(defpath -> {
                String ident = ">>> ".repeat(defpath.directPath().size());
                String nodeText = switch (defpath.definition()) {
                    case Grammar.Term(var txt) -> "Term " + Ascii.Color.Blue.foreground() + Ascii.bold + txt + Ascii.reset;
                    case Grammar.Ref(var ref) -> "Ref " + Ascii.italicOn + Ascii.Color.Magenta.foreground() + ref + Ascii.reset;
                    case Grammar.Repeat r -> "Repeat";
                    case Grammar.Alternative a -> "Alternative";
                    case Grammar.Sequence s -> "Sequence";
                };
                System.out.println(
                    Ascii.Color.White.foreground() +
                        ident +
                        Ascii.Color.Default.foreground() +
                        nodeText +
                        Ascii.Color.White.foreground() + " [" + rule.indexOf(defpath.definition()) + "]" + Ascii.reset
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

    @SuppressWarnings({"SimplifiableAssertion", "Convert2MethodRef"})
    @Test
    public void recusiveFind() {
        var gr = validMath;

        var recursiveRefs = RecursiveRef.find(gr);
        recursiveRefs.each(rr -> System.out.println(rr));

        assertTrue(recursiveRefs.size()>0);

        Map<String, List<RecursiveRef>> groupByRule = new HashMap<>();
        recursiveRefs.each(rr -> {
            groupByRule.computeIfAbsent(rr.startRule().map(r -> r.name()).orElse("?"), _x -> new ArrayList<>()).add(rr);
        });

        // recursive rule exp, path exp/ref(sum)[0] > sum/ref(mul)[2] > mul/ref(atom)[2] > atom/ref(exp)[4]
        // recursive rule exp, path exp/ref(sum)[0] > sum/ref(mul)[2] > mul/ref(exp)[4]
        // recursive rule exp, path exp/ref(sum)[0] > sum/ref(exp)[4]
        assertTrue( groupByRule.get("exp").size()==3 );

        // recursive rule sum, path sum/ref(mul)[2] > mul/ref(atom)[2] > atom/ref(exp)[4] > exp/ref(sum)[0]
        assertTrue( groupByRule.get("sum").size()==1 );

        // recursive rule mul, path mul/ref(atom)[2] > atom/ref(exp)[4] > exp/ref(sum)[0] > sum/ref(mul)[2]
        // recursive rule mul, path mul/ref(atom)[2] > atom/ref(exp)[4] > exp/ref(sum)[0] > sum/ref(mul)[5]
        assertTrue( groupByRule.get("mul").size()==2 );

        // recursive rule atom, path atom/ref(exp)[4] > exp/ref(sum)[0] > sum/ref(mul)[2] > mul/ref(atom)[2]
        // recursive rule atom, path atom/ref(exp)[4] > exp/ref(sum)[0] > sum/ref(mul)[2] > mul/ref(atom)[5]
        assertTrue( groupByRule.get("atom").size()==2 );

        var leftRecCount = recursiveRefs.foldLeft(0, (acc,it) -> acc + (it.isLeftRecursion() ? 1 : 0) );
        assertTrue(leftRecCount < 1);
    }
}
