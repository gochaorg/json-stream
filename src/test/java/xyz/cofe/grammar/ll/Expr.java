package xyz.cofe.grammar.ll;

import xyz.cofe.grammar.ll.bind.Rule;

public sealed interface Expr permits IntNumber,
                                     MathGrammar.Atom,
                                     MathGrammar.MultipleOperation,
                                     MathGrammar.PlusOperation {
    @Rule
    static Expr parse(MathGrammar.PlusOperation expr) {
        return expr;
    }
}
