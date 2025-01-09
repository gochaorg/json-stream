package xyz.cofe.json.stream.query;

import xyz.cofe.coll.im.Fn1;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Result;
import xyz.cofe.coll.im.iter.EmptyIterator;
import xyz.cofe.coll.im.iter.ExtIterable;
import xyz.cofe.coll.im.iter.SingleIterator;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.ast.AstWriter;
import xyz.cofe.json.stream.token.CharPointer;
import xyz.cofe.json.stream.token.CloseSquare;
import xyz.cofe.json.stream.token.Comma;
import xyz.cofe.json.stream.token.DummyCharPointer;
import xyz.cofe.json.stream.token.IndentTokenWriter;
import xyz.cofe.json.stream.token.OpenSquare;
import xyz.cofe.json.stream.token.SimpleTokenWriter;
import xyz.cofe.json.stream.token.TokenWriter;

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Запрос к данным json, источник данных - это итератор, так что потенциальное
 * @param <S> Тип исходника
 * @param <SELF> Собственный тип
 */
public abstract class QuerySet<S extends CharPointer<S>, SELF extends QuerySet<S, SELF>> implements Iterable<Ast<S>>{
    protected ExtIterable<Ast<S>> source;

    /**
     * Конструктор
     * @param source источник данных
     */
    public QuerySet(ExtIterable<Ast<S>> source) {
        if (source == null) throw new IllegalArgumentException("source==null");
        this.source = source;
    }

    /**
     * Конструктор
     * @param source источник данных
     */
    public QuerySet(Iterable<Ast<S>> source) {
        if (source == null) throw new IllegalArgumentException("source==null");
        this.source = ExtIterable.from(source);
    }

    /**
     * Конструктор
     * @param source источник данных
     */
    @SafeVarargs
    public QuerySet(Ast<S>... source) {
        if (source == null) throw new IllegalArgumentException("source==null");
        this.source = ExtIterable.from(Arrays.asList(source));
    }

    /**
     * Конструктор
     * @param source источник данных
     */
    public QuerySet(Stream<Ast<S>> source) {
        if (source == null) throw new IllegalArgumentException("source==null");
        this.source = ExtIterable.from(source.toList());
    }

    private <A> ExtIterable<A> toIterable(Fn1<Ast<S>, Optional<A>> map) {
        return source.fmap(v -> {
            var optValue = map.apply(v);
            if (optValue.isPresent()) return new SingleIterator<>(optValue.get());
            return new EmptyIterator<>();
        });
    }

    /**
     * Возвращает источник данных
     * @return источник данных
     */
    public ExtIterable<Ast<S>> source() {return source;}

    @Override
    public Iterator<Ast<S>> iterator() {
        return source().iterator();
    }

    //region toStrings() toInts() ....

    /**
     * Попытка отфильтровать и вернуть тип String из {@link Ast.StringAst}
     * @return Итератор по строкам
     */
    public ExtIterable<String> toStrings() {
        return toIterable(Ast::asString);
    }

    /**
     * Попытка отфильтровать и вернуть тип String из {@link Ast.IdentAst}
     * @return Итератор по строкам
     */
    public ExtIterable<String> toIdents() {
        return toIterable(Ast::asIdent);
    }

    /**
     * Попытка отфильтровать и вернуть тип String из {@link Ast.IdentAst} и/или {@link Ast.StringAst}
     * @return Итератор по строкам
     */
    public ExtIterable<String> toTexts() {
        return toIterable(Ast::asText);
    }

    /**
     * Попытка отфильтровать и вернуть тип Number из {@link Ast.NumberAst.LongAst} | {@link Ast.NumberAst.IntAst} | {@link Ast.NumberAst.BigIntAst} | {@link Ast.NumberAst.DoubleAst}
     * @return Итератор по числам
     */
    public ExtIterable<Number> toNumbers() {
        return toIterable(Ast::asNumber);
    }

    /**
     * Попытка отфильтровать и вернуть тип Integer из {@link Ast.NumberAst.IntAst}
     * @return Итератор по числам
     */
    public ExtIterable<Integer> toInts() {
        return toIterable(Ast::asInt);
    }

    /**
     * Попытка отфильтровать и вернуть тип Double из {@link Ast.NumberAst.DoubleAst}
     * @return Итератор по числам
     */
    public ExtIterable<Double> toDoubles() {
        return toIterable(Ast::asDouble);
    }

    /**
     * Попытка отфильтровать и вернуть тип Long из {@link Ast.NumberAst.LongAst}
     * @return Итератор по числам
     */
    public ExtIterable<Long> toLongs() {
        return toIterable(Ast::asLong);
    }

    /**
     * Попытка отфильтровать и вернуть тип BigInteger из {@link Ast.NumberAst.BigIntAst}
     * @return Итератор по числам
     */
    public ExtIterable<BigInteger> toBigInts() {
        return toIterable(Ast::asBigInt);
    }

    /**
     * Попытка отфильтровать и вернуть тип Result.NoValue из {@link Ast.NullAst}
     * @return Итератор по null
     */
    public ExtIterable<Result.NoValue> toNulls() {
        return toIterable(Ast::asNull);
    }

    /**
     * Попытка отфильтровать и вернуть тип Boolean из {@link Ast.BooleanAst}
     * @return Итератор по boolean
     */
    public ExtIterable<Boolean> toBooleans() {
        return toIterable(Ast::asBoolean);
    }

    /**
     * Попытка отфильтровать и вернуть тип ImList<Ast<S>> из {@link Ast.ArrayAst}
     * @return Итератор по ImList
     */
    public ExtIterable<ImList<Ast<S>>> toLists() {
        return toIterable(Ast::asList);
    }

    /**
     * Попытка отфильтровать и вернуть тип Ast.ObjectAst<S> из {@link Ast.ObjectAst}
     * @return Итератор по ImList
     */
    public ExtIterable<Ast.ObjectAst<S>> toObjects() {
        return toIterable(Ast::asObject);
    }
    //endregion

    protected abstract SELF create(ExtIterable<Ast<S>> source);

    /**
     * Операция flatMap
     * @param mapper преобразование
     * @return выборка
     */
    public SELF fmap(Fn1<Ast<S>, Iterator<Ast<S>>> mapper) {
        if (mapper == null) throw new IllegalArgumentException("mapper==null");
        return create(source().fmap(mapper));
    }

    /**
     * Операция map
     * @param mapper преобразование
     * @return выборка
     */
    public SELF map(Fn1<Ast<S>, Ast<S>> mapper) {
        if (mapper == null) throw new IllegalArgumentException("mapper==null");
        return create(source().map(mapper));
    }

    /**
     * Фильтрация выборки
     * @param predicate фильтр
     * @return выборка
     */
    public SELF filter(Predicate<Ast<S>> predicate) {
        if (predicate == null) throw new IllegalArgumentException("predicate==null");
        return create(source().filter(predicate));
    }

    /**
     * Выбирает первые count записей
     * @param count кол-во возвращаемых записей
     * @return выборка
     */
    public SELF take(long count) {
        return create(source().take(count));
    }

    /**
     * Пропуск первых count записей
     * @param count кол-во пропускаемых записей
     * @return выборка
     */
    public SELF skip(long count) {
        return create(source().skip(count));
    }

    /**
     * Добавляет к выборке еще выборку в конец
     * @param other другая выборка
     * @return выборка
     */
    public SELF append(Iterable<Ast<S>> other){
        if( other==null ) throw new IllegalArgumentException("other==null");
        return create(source().append(other));
    }

    /**
     * Добавляет к выборке еще выборку в начало
     * @param other другая выборка
     * @return выборка
     */
    public SELF prepend(Iterable<Ast<S>> other){
        if( other==null ) throw new IllegalArgumentException("other==null");
        return create(source().prepend(other));
    }

    public SELF keyValueFlatMap(Function<Ast.KeyValue<S>,ImList<Ast<S>>> fieldMapper){
        if( fieldMapper==null ) throw new IllegalArgumentException("fieldMapper==null");
        return create(toObjects().fmap( obj -> {
            ImList<Ast<S>> lst = ImList.of();
            for( var kevValue : obj.values() ){
                lst = fieldMapper.apply(kevValue).prepend(lst);
            }
            return lst.iterator();
        }));
    }

    public SELF get(Predicate<String> keyFilter) {
        if( keyFilter==null ) throw new IllegalArgumentException("keyFilter==null");
        return keyValueFlatMap( kv -> {
            if( keyFilter.test(kv.key().value()) ){
                return ImList.of(kv.value());
            }
            return ImList.of();
        });
    }

    public SELF get(String key){
        if( key==null ) throw new IllegalArgumentException("key==null");
        return get(key::equals);
    }

    public SELF get(Pattern pattern){
        if( pattern==null ) throw new IllegalArgumentException("pattern==null");
        return get(k -> pattern.matcher(k).matches());
    }

    public SELF arrayFlatMap(BiFunction<Integer,Ast<S>,ImList<Ast<S>>> arrayValue){
        if( arrayValue==null ) throw new IllegalArgumentException("arrayValue==null");
        return create(toLists().fmap( lst -> {
            ImList<Ast<S>> res = ImList.of();
            var idx = -1;
            for( var item : lst ){
                idx++;
                res = arrayValue.apply(idx,item).prepend(res);
            }
            return res.iterator();
        }));
    }

    public SELF array(Predicate<Integer> indexPredicate){
        if( indexPredicate==null ) throw new IllegalArgumentException("indexPredicate==null");
        return arrayFlatMap( (idx,value) -> indexPredicate.test(idx) ? ImList.of(value) : ImList.of() );
    }

    private Function<Appendable,TokenWriter> createTokenWriter = out ->
        //new IndentTokenWriter( new SimpleTokenWriter(out) );
        new SimpleTokenWriter(out);

    @SuppressWarnings("rawtypes")
    public SELF tokenWriter(Function<Appendable,TokenWriter> writer){
        if( writer==null ) throw new IllegalArgumentException("writer==null");
        var qs = create(source);
        ((QuerySet)qs).createTokenWriter = writer;
        return qs;
    }

    private TokenWriter tokenWriter(Appendable out){
        return createTokenWriter.apply(out);
    }

    @SuppressWarnings("rawtypes")
    private static final OpenSquare openSquareToken = new OpenSquare<>(DummyCharPointer.instance, DummyCharPointer.instance);

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final CloseSquare closeSquareToken = new CloseSquare(DummyCharPointer.instance, DummyCharPointer.instance);

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Comma commaToken = new Comma(DummyCharPointer.instance, DummyCharPointer.instance);

    public void write(TokenWriter out){
        if( out==null ) throw new IllegalArgumentException("out==null");
        out.write(openSquareToken);
        var idx = -1;
        for( var a : source ){
            idx++;
            if( idx>0 ){
                out.write(commaToken);
            }

            AstWriter.write(out, a);
        }
        out.write(closeSquareToken);
    }

    public SELF pretty(){
        return tokenWriter(
            out -> new IndentTokenWriter( new SimpleTokenWriter(out) )
        );
    }

    public SELF pretty(boolean pretty){
        return tokenWriter(
            out -> pretty
                ? new IndentTokenWriter( new SimpleTokenWriter(out) )
                : new SimpleTokenWriter(out)
        );
    }

    public String toString(){
        StringWriter sw = new StringWriter();
        write(tokenWriter(sw));
        return sw.toString();
    }
}
