package xyz.cofe.json.stream.query;

import xyz.cofe.coll.im.iter.ExtIterable;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.token.CharPointer;

import java.util.stream.Stream;

public class QuerySetInf<S extends CharPointer<S>> extends QuerySet<S,QuerySetInf<S>> {
    public QuerySetInf(ExtIterable<Ast<S>> source) {
        super(source);
    }

    public QuerySetInf(Iterable<Ast<S>> source) {
        super(source);
    }

    @SafeVarargs
    public QuerySetInf(Ast<S>... source) {
        super(source);
    }

    public QuerySetInf(Stream<Ast<S>> source) {
        super(source);
    }

    @Override
    protected QuerySetInf<S> create(ExtIterable<Ast<S>> source) {
        return new QuerySetInf<>(source);
    }
}
