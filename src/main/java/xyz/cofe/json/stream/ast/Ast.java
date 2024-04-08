package xyz.cofe.json.stream.ast;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.token.BigIntToken;
import xyz.cofe.json.stream.token.CharPointer;
import xyz.cofe.json.stream.token.CloseParentheses;
import xyz.cofe.json.stream.token.CloseSquare;
import xyz.cofe.json.stream.token.DoubleToken;
import xyz.cofe.json.stream.token.FalseToken;
import xyz.cofe.json.stream.token.IdentifierToken;
import xyz.cofe.json.stream.token.IntToken;
import xyz.cofe.json.stream.token.LongToken;
import xyz.cofe.json.stream.token.NullToken;
import xyz.cofe.json.stream.token.OpenParentheses;
import xyz.cofe.json.stream.token.OpenSquare;
import xyz.cofe.json.stream.token.StringToken;
import xyz.cofe.json.stream.token.TrueToken;

public sealed interface Ast<S extends CharPointer<S>> {
    sealed interface Primitive {}

    sealed interface NumberAst<S extends CharPointer<S>> extends Ast<S>,
                                                                 Primitive {
        record DoubleAst<S extends CharPointer<S>>(DoubleToken<S> token) implements NumberAst<S> {}
        record IntAst<S extends CharPointer<S>>(IntToken<S> token) implements NumberAst<S> {}
        record LongAst<S extends CharPointer<S>>(LongToken<S> token) implements NumberAst<S> {}
        record BigIntAst<S extends CharPointer<S>>(BigIntToken<S> token) implements NumberAst<S> {}
    }

    sealed interface BooleanAst<S extends CharPointer<S>> extends Ast<S>,
                                                                  Primitive {
        record TrueAst<S extends CharPointer<S>>(TrueToken<S> token) implements BooleanAst<S> {}
        record FalseAst<S extends CharPointer<S>>(FalseToken<S> token) implements BooleanAst<S> {}
    }

    record NullAst<S extends CharPointer<S>>(NullToken<S> token) implements Ast<S>,
                                                                            Primitive {}

    record StringAst<S extends CharPointer<S>>(StringToken<S> token) implements Ast<S>,
                                                                                Primitive,
                                                                                Key {}

    record IdentAst<S extends CharPointer<S>>(IdentifierToken<S> token) implements Ast<S>,
                                                                                   Primitive,
                                                                                   Key {}

    sealed interface Key {}
    record KeyValue<S extends CharPointer<S>>(Key key, Ast<S> value) implements Ast<S> {}

    record ObjectAst<S extends CharPointer<S>>(
        ImList<KeyValue<S>> values,
        OpenParentheses<S> begin,
        CloseParentheses<S> end
    ) implements Ast<S> {}

    record ArrayAst<S extends CharPointer<S>>(
        ImList<Ast<S>> values, OpenSquare<S> begin, CloseSquare<S> end) implements Ast<S> {}
}
