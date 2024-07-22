package xyz.cofe.grammar;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Tuple2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static xyz.cofe.grammar.Grammar.*;

/*

term      - min weight = 1
repeat(r) - min weight = 0

seq { s[i] }
  min weight = sum of ( min weight ( s[i] ) )

alt { a[i] }
  min weight = min of ( a[i] )

ref { r }
  min weight = min of ( r )
  for recursive compute minimal before recursive reference

---------------------
rule = a <b> // w[0]=sum(w(a),w(<b>))=sum(1,2)=1+2=3
     | <c> e // w[1]=0+1=1
             // w=min(w[0],w[1])=min(3,1)=1

b = f g    // w=2
c = { f }  // w=0

---------------------
exp = sum

sum = mul + exp
    | mul

mul = atom * exp // w1 = w(atom) + w(*) + w(exp)
                 //    = 1 + 1 + ?
                 //    = 2 + ?
    | atom       // w2 = w(atom)
                 // w = w1 min w2
                      = 2+? min 1
                      = 1

atom = n     // w1 = w(n)
             //    = 1
     | (exp) // w2 = 1 + ? + 1 = 2 + ?
             // w  = w1 min w2 = 1 min 2+? = 1
-----------------------------
exp = g
      ( a b c d
      | a b
      )
      | d e f

    = ( g
        ( a b c d
        | a b
        )
      )
      |
      (
        d e f
      )

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
public class Weight {
    private final Grammar grammar;

    public Weight(Grammar grammar) {
        if (grammar == null) throw new IllegalArgumentException("grammar==null");
        this.grammar = grammar;
    }

    public int weightOf(Grammar.Rule rule) {
        if (rule == null) throw new IllegalArgumentException("rule==null");
        return weightOf(rule, new HashMap<>(), ImList.of());
    }

    public int weightOf(Grammar.Definition definition) {
        if (definition == null) throw new IllegalArgumentException("definition==null");
        return compute(definition, new HashMap<>(), ImList.of(definition));
    }

    private int weightOf(Grammar.Rule rule, HashMap<Rule, Integer> visited, ImList<Object> path) {
        if (rule == null) throw new IllegalArgumentException("rule==null");
        if (visited.containsKey(rule)) return visited.get(rule);

        visited.put(rule, 0);

        var w = compute(rule.definition(), visited, path.prepend(rule));
        visited.put(rule, w);

        return w;
    }

    private int compute(Definition definition, HashMap<Rule, Integer> visited, ImList<Object> path) {
        int weight = 0;

        if( definition instanceof Repeat ){
            weight = 0;
        }else if( definition instanceof Term ){
            weight = 1;
        }else if( definition instanceof Sequence seq ){
            weight = seq.seq().map(def -> compute(def, visited, path.prepend(def))).foldLeft(0, Integer::sum);
        }else if( definition instanceof Alternative alt ){
            weight = alt.alt().map(def -> compute(def, visited, path.prepend(def)))
                .foldLeft(Integer.MAX_VALUE, (acc, it) -> it < acc ? it : acc);

            if (weight == Integer.MAX_VALUE) throw new IllegalArgumentException("");
        }else if( definition instanceof Ref r ){
            weight = grammar.rule(r.name()).map(rule -> {
                    var w = visited.get(rule);
                    if (w != null) return w;

                    return weightOf(rule, visited, path.prepend(rule));
                })
                .foldLeft(Integer.MAX_VALUE, (acc, it) -> it < acc ? it : acc);
        }
        return weight;
    }
}
