package xyz.cofe.json.stream.query;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Result;
import xyz.cofe.coll.im.iter.ExtIterable;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.token.CharPointer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class QuerySetFin<S extends CharPointer<S>> extends QuerySet<S, QuerySetFin<S>> {
    public QuerySetFin(ExtIterable<Ast<S>> source) {
        super(source);
    }

    public QuerySetFin(Iterable<Ast<S>> source) {
        super(source);
    }

    @SafeVarargs
    public QuerySetFin(Ast<S>... source) {
        super(source);
    }

    public QuerySetFin(Stream<Ast<S>> source) {
        super(source);
    }

    @Override
    protected QuerySetFin<S> create(ExtIterable<Ast<S>> source) {
        return new QuerySetFin<>(source);
    }

    //region count() : long
    private volatile Long count;

    public long count() {
        if (count != null) return count;
        synchronized (this) {
            if (count != null) return count;
            long c = 0;
            for (var e : source()) {
                c++;
            }
            count = c;
            return count;
        }
    }
    //endregion

    //region toXXXList() firstXXX()
    public ImList<String> toStringList() {
        return ImList.from(toStrings());
    }

    public Optional<String> firstString() {
        return toStrings().first();
    }

    public ImList<String> toIdentList() {
        return ImList.from(toIdents());
    }

    public Optional<String> firstIdent() {
        return toIdents().first();
    }

    public ImList<Integer> toIntList() {
        return ImList.from(toInts());
    }

    public Optional<Integer> firstInt() {
        return toInts().first();
    }

    public ImList<Double> toDoubleList() {
        return ImList.from(toDoubles());
    }

    public Optional<Double> firstDouble() {
        return toDoubles().first();
    }

    public ImList<Long> toLongList() {
        return ImList.from(toLongs());
    }

    public Optional<Long> firstLong() {
        return toLongs().first();
    }

    public ImList<BigInteger> toBigIntList() {
        return ImList.from(toBigInts());
    }

    public Optional<BigInteger> firstBigInt() {
        return toBigInts().first();
    }

    public ImList<Result.NoValue> toNullList() {
        return ImList.from(toNulls());
    }

    public Optional<Result.NoValue> firstNull() {
        return toNulls().first();
    }

    public ImList<Boolean> toBooleanList() {
        return ImList.from(toBooleans());
    }

    public Optional<Boolean> firstBoolean() {
        return toBooleans().first();
    }

    public ImList<ImList<Ast<S>>> toListList() {
        return ImList.from(toLists());
    }

    public Optional<ImList<Ast<S>>> firstList() {
        return toLists().first();
    }

    public ImList<Ast.ObjectAst<S>> toObjectList() {
        return ImList.from(toObjects());
    }

    public Optional<Ast.ObjectAst<S>> firstObject() {
        return toObjects().first();
    }
    //endregion

    public QuerySetFin<S> reverse(){
        Iterable<Ast<S>> me = this;
        return new QuerySetFin<>(
            new ExtIterable<Ast<S>>() {
                private Iterator<Ast<S>> iter;

                @Override
                public Iterator<Ast<S>> iterator() {
                    if( iter!=null )return iter;
                    ImList<Ast<S>> lst = ImList.of();

                    for( var a : me ){
                        lst = lst.prepend(a);
                    }

                    iter = lst.iterator();
                    return iter;
                }
            }
        );
    }

    public QuerySetFin<S> sort( Comparator<? super Ast<S>> cmp ){
        if( cmp==null ) throw new IllegalArgumentException("cmp==null");

        Iterable<Ast<S>> me = this;
        return new QuerySetFin<>(
            new ExtIterable<Ast<S>>() {
                private Iterator<Ast<S>> iter;

                @Override
                public Iterator<Ast<S>> iterator() {
                    if( iter!=null )return iter;

                    List<Ast<S>> entries = new ArrayList<>();
                    me.forEach(entries::add);
                    entries.sort(cmp);

                    iter = entries.iterator();
                    return iter;
                }
            }
        );
    }

    public <B extends Comparable<B>> QuerySetFin<S> sort( Function<? super Ast<S>,B> cmp ){
        if( cmp==null ) throw new IllegalArgumentException("cmp==null");
        return sort( (a,b) -> {
            var c0 = cmp.apply(a);
            var c1 = cmp.apply(b);
            return c0.compareTo(c1);
        } );
    }

    public <B> B foldLeft(B initial, BiFunction<B,Ast<S>,B> folder){
        var res = initial;
        for( var a : source ){
            res = folder.apply(res,a);
        }
        return res;
    }
}
