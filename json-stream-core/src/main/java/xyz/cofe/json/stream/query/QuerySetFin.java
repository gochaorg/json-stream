package xyz.cofe.json.stream.query;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Result;
import xyz.cofe.coll.im.iter.ExtIterable;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.ast.AstParser;
import xyz.cofe.json.stream.token.CharPointer;
import xyz.cofe.json.stream.token.StringPointer;

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
    public static QuerySetFin<StringPointer> fromJson( String json ){
        if( json==null ) throw new IllegalArgumentException("json==null");
        return new QuerySetFin<>(
            AstParser.parse(json)
        );
    }

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

    private ImList<Ast<S>> list;
    public ImList<Ast<S>> toList(){
        if( list!=null )return list;
        list = source.toImList();
        return list;
    }

    public Optional<Ast<S>> get(int index){
        if( index<0 )return Optional.empty();
        return toList().get( (int)index );
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

    public ImList<String> toTextList() {
        return ImList.from(toTexts());
    }

    public Optional<String> firstText() {
        return toTexts().first();
    }

    public ImList<Number> toNumberList() {
        return ImList.from(toNumbers());
    }

    public Optional<Number> firstNumber() {
        return toNumbers().first();
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
                private ImList<Ast<S>> reversedEntries;
                private ImList<Ast<S>> reversedEntries(){
                    if( reversedEntries!=null )return reversedEntries;

                    ImList<Ast<S>> lst = ImList.of();

                    for( var a : me ){
                        lst = lst.prepend(a);
                    }

                    reversedEntries = lst;
                    return reversedEntries;
                }

                @Override
                public Iterator<Ast<S>> iterator() {
                    return reversedEntries().iterator();
                }
            }
        );
    }

    public QuerySetFin<S> sort( Comparator<? super Ast<S>> cmp ){
        if( cmp==null ) throw new IllegalArgumentException("cmp==null");

        Iterable<Ast<S>> me = this;
        return new QuerySetFin<>(
            new ExtIterable<Ast<S>>() {
                private List<Ast<S>> sortedEntries;

                private List<Ast<S>> sortedEntries(){
                    if( sortedEntries!=null )return sortedEntries;

                    List<Ast<S>> entries = new ArrayList<>();
                    me.forEach(entries::add);
                    entries.sort(cmp);

                    sortedEntries = entries;
                    return sortedEntries;
                }

                @Override
                public Iterator<Ast<S>> iterator() {
                    return sortedEntries().iterator();
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
