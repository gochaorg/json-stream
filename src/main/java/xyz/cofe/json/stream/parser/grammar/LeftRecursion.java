package xyz.cofe.json.stream.parser.grammar;

import xyz.cofe.coll.im.ImList;

public record LeftRecursion() {

/*

1)
A ::= A

Rule(A){ Ref(A) }

2)
A ::= B
B ::= A

Rule(A){ Ref(B) }
Rule(B){ Ref(A) }

3)
A ::= B | A

Rule(A){
  Alt(
    Ref(B),
    Ref(A)
  )
}

4)
A ::= { B } A

Rule(A){
  Seq(
    Repeat( Ref(B) ),
    Ref(A)
  )
}

*/

    public static ImList<LeftRecursion> find(Grammar grammar) {
        return null;
    }
}
