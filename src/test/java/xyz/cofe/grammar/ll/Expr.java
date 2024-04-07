package xyz.cofe.grammar.ll;

import xyz.cofe.grammar.ll.bind.Rule;

public sealed interface Expr permits Ident,
                                     IntNumber,
                                     MathGrammar.AssignExpr,
                                     MathGrammar.Atom,
                                     MathGrammar.MultipleOperation,
                                     MathGrammar.PlusOperation {
    @Rule(order = 1)
    static Expr parse(MathGrammar.AssignExpr assignExpr){
        return assignExpr;
    }

    @Rule(order = 10)
    static Expr parse(MathGrammar.PlusOperation expr) {
        return expr;
    }
}
