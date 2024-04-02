package xyz.cofe.grammar.ll;

import xyz.cofe.grammar.ll.bind.TermBind;

public enum Parentheses {
    @TermBind("(")
    Open,

    @TermBind(")")
    Close;
}
