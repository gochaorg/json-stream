package xyz.cofe.grammar;

import org.junit.jupiter.api.Test;
import xyz.cofe.coll.im.ImList;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class WeightTest {

    /*

    = ( g          // w1
        ( a b c d  //   w2
        | a b      //   w3
        )          // w4
      )            // w5
      |
      (
        d e f      // w6
      )

    0 --- w5 -- w1 -- w2
       |        |
       |        +---- w3
       |
       |- w6 --------

   0+         1+           3+           3(min)
     w5       1+           3=(1+2)
       w1     1
         w2       4
         w3       2 (min)
     w6                              3

     */

    @Test
    public void test1(){
        var root = new Grammar.Alternative(ImList.of(
            new Grammar.Sequence(ImList.of(
                new Grammar.Term("g"),
                new Grammar.Alternative(
                    ImList.of(
                        new Grammar.Sequence(ImList.of(
                            new Grammar.Term("a"),
                            new Grammar.Term("b"),
                            new Grammar.Term("c"),
                            new Grammar.Term("d")
                        )),
                        new Grammar.Sequence(ImList.of(
                            new Grammar.Term("a"),
                            new Grammar.Term("b")
                        ))
                    )
                )
            )),
            new Grammar.Sequence(ImList.of(
                new Grammar.Term("d"),
                new Grammar.Term("e"),
                new Grammar.Term("f")
            ))
        ));

        var rule = new Grammar.Rule("root", root);
        var gr = new Grammar(ImList.of(rule));

        var weight = new Weight(gr);
        var w = weight.weightOf(rule);
        System.out.println(w);

        assertTrue(w==3);
    }

    @Test
    public void test_atom(){
        var gr = GrammarBuildTest.validMath;
        var rule = gr.firstRule("atom").get();

        var weight = new Weight(gr);
        var w = weight.weightOf(rule);
        System.out.println(w);
    }

    @Test
    public void test_mul(){
        var gr = GrammarBuildTest.validMath;
        var rule = gr.firstRule("mul").get();

        var weight = new Weight(gr);
        var w = weight.weightOf(rule);
        System.out.println(w);
    }
}
