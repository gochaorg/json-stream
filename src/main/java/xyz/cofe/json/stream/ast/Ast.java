package xyz.cofe.json.stream.ast;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Result;
import xyz.cofe.json.stream.token.BigIntToken;
import xyz.cofe.json.stream.token.CharPointer;
import xyz.cofe.json.stream.token.CloseParentheses;
import xyz.cofe.json.stream.token.CloseSquare;
import xyz.cofe.json.stream.token.DoubleToken;
import xyz.cofe.json.stream.token.DummyCharPointer;
import xyz.cofe.json.stream.token.FalseToken;
import xyz.cofe.json.stream.token.IdentifierToken;
import xyz.cofe.json.stream.token.IntToken;
import xyz.cofe.json.stream.token.LongToken;
import xyz.cofe.json.stream.token.MLComment;
import xyz.cofe.json.stream.token.NullToken;
import xyz.cofe.json.stream.token.OpenParentheses;
import xyz.cofe.json.stream.token.OpenSquare;
import xyz.cofe.json.stream.token.SLComment;
import xyz.cofe.json.stream.token.StringToken;
import xyz.cofe.json.stream.token.TrueToken;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Все возможные узлы AST дерева Json
 *
 * @param <S> Источник JSON
 */
public sealed interface Ast<S extends CharPointer<S>> {
    default Optional<String> asString(){
        if( this instanceof StringAst<?> s ) return Optional.of(s.value());
        return Optional.empty();
    }

    default Optional<Integer> asInt(){
        if( this instanceof NumberAst.IntAst<?> a ) return Optional.of(a.value());
        return Optional.empty();
    }

    default Optional<Double> asDouble(){
        if( this instanceof NumberAst.DoubleAst<?> a ) return Optional.of(a.value());
        return Optional.empty();
    }

    default Optional<Long> asLong(){
        if( this instanceof NumberAst.LongAst<?> a ) return Optional.of(a.value());
        return Optional.empty();
    }

    default Optional<BigInteger> asBigInt(){
        if( this instanceof NumberAst.BigIntAst<?> a ) return Optional.of(a.value());
        return Optional.empty();
    }

    default Optional<Result.NoValue> asNull(){
        if( this instanceof Ast.NullAst<?> a ) return Optional.of(Result.NoValue.instance);
        return Optional.empty();
    }

    default Optional<String> asIdent(){
        if( this instanceof Ast.IdentAst<?> a ) return Optional.of(a.value());
        return Optional.empty();
    }

    default Optional<Boolean> asBoolean(){
        if( this instanceof Ast.BooleanAst<?> a ) return Optional.of(a.value());
        return Optional.empty();
    }

    default Optional<ImList<Ast<S>>> asList(){
        if( this instanceof Ast.ArrayAst<S> a ) return Optional.of(a.values());
        return Optional.empty();
    }

    default Optional<Ast.ObjectAst<S>> asObject(){
        if( this instanceof Ast.ObjectAst<S> a ) return Optional.of(a);
        return Optional.empty();
    }

    /**
     * Комментарий
     *
     * @param <S>
     */
    sealed interface Comment<S extends CharPointer<S>> extends Ast<S> {
        /**
         * Многострочный комментарий
         *
         * @param token
         * @param <S>
         */
        record MultiLine<S extends CharPointer<S>>(MLComment<S> token) implements Comment<S> {
            public static MultiLine<DummyCharPointer> create(String text) {
                if (text == null) throw new IllegalArgumentException("text==null");
                return new MultiLine<>(
                    new MLComment<>(text, DummyCharPointer.instance, DummyCharPointer.instance)
                );
            }
        }

        /**
         * Однострочный комментарий
         *
         * @param token
         * @param <S>
         */
        record SingleLine<S extends CharPointer<S>>(SLComment<S> token) implements Comment<S> {
            public static SingleLine<DummyCharPointer> create(String text) {
                if (text == null) throw new IllegalArgumentException("text==null");
                return new SingleLine<>(new SLComment<>(text, DummyCharPointer.instance, DummyCharPointer.instance));
            }
        }
    }

    /**
     * Примитивное
     */
    sealed interface Primitive {}

    /**
     * Число
     *
     * @param <S>
     */
    sealed interface NumberAst<S extends CharPointer<S>> extends Ast<S>,
                                                                 Primitive {
        /**
         * Число плавающее
         *
         * @param token
         * @param <S>
         */
        record DoubleAst<S extends CharPointer<S>>(
            DoubleToken<S> token
        ) implements NumberAst<S> {
            public static DoubleAst<DummyCharPointer> create(double value) {
                return new DoubleAst<>(new DoubleToken<>(value, DummyCharPointer.instance, DummyCharPointer.instance));
            }

            public double value(){
                return token().value();
            }
        }

        /**
         * Целое число (32 бит)
         *
         * @param token
         * @param <S>
         */
        record IntAst<S extends CharPointer<S>>(IntToken<S> token) implements NumberAst<S> {
            public static IntAst<DummyCharPointer> create(int value) {
                return new IntAst<>(new IntToken<>(value, DummyCharPointer.instance, DummyCharPointer.instance));
            }

            public int value(){
                return token().value();
            }
        }

        /**
         * Целое число (64 бит)
         *
         * @param token
         * @param <S>
         */
        record LongAst<S extends CharPointer<S>>(LongToken<S> token) implements NumberAst<S> {
            public static LongAst<DummyCharPointer> create(long value) {
                return new LongAst<>(new LongToken<>(value, DummyCharPointer.instance, DummyCharPointer.instance));
            }

            public long value(){
                return token().value();
            }
        }

        /**
         * Целое числов (дофига бит)
         *
         * @param token
         * @param <S>
         */
        record BigIntAst<S extends CharPointer<S>>(BigIntToken<S> token) implements NumberAst<S> {
            public static BigIntAst<DummyCharPointer> create(BigInteger value) {
                if (value == null) throw new IllegalArgumentException("value==null");
                return new BigIntAst<>(new BigIntToken<>(value, DummyCharPointer.instance, DummyCharPointer.instance));
            }

            public BigInteger value(){
                return token().value();
            }
        }
    }

    /**
     * Булево значение
     *
     * @param <S>
     */
    sealed interface BooleanAst<S extends CharPointer<S>> extends Ast<S>,
                                                                  Primitive {

        /**
         * True значение
         *
         * @param token
         * @param <S>
         */
        record TrueAst<S extends CharPointer<S>>(TrueToken<S> token) implements BooleanAst<S> {
            public static TrueAst<DummyCharPointer> create() {
                return new TrueAst<>(new TrueToken<>(DummyCharPointer.instance, DummyCharPointer.instance));
            }
        }

        /**
         * False значение
         *
         * @param token
         * @param <S>
         */
        record FalseAst<S extends CharPointer<S>>(FalseToken<S> token) implements BooleanAst<S> {
            public static FalseAst<DummyCharPointer> create() {
                return new FalseAst<>(new FalseToken<>(DummyCharPointer.instance, DummyCharPointer.instance));
            }
        }

        public static BooleanAst<DummyCharPointer> create(boolean value) {
            return value ? TrueAst.create() : FalseAst.create();
        }

        default public boolean value(){
            return this instanceof BooleanAst.TrueAst<S>;
        }
    }

    /**
     * Null значение
     *
     * @param token
     * @param <S>
     */
    record NullAst<S extends CharPointer<S>>(NullToken<S> token) implements Ast<S>,
                                                                            Primitive {
        public static NullAst<DummyCharPointer> create() {
            return new NullAst<DummyCharPointer>(new NullToken<>(DummyCharPointer.instance, DummyCharPointer.instance));
        }
    }

    /**
     * Строка
     *
     * @param token
     * @param <S>
     */
    record StringAst<S extends CharPointer<S>>(StringToken<S> token) implements Ast<S>,
                                                                                Primitive,
                                                                                Key<S> {
        public StringAst {
            if( token==null ) throw new IllegalArgumentException("token==null");
        }

        public static StringAst<DummyCharPointer> create(String value) {
            if (value == null) throw new IllegalArgumentException("value==null");
            return new StringAst<>(new StringToken<>(value, DummyCharPointer.instance, DummyCharPointer.instance));
        }

        public String value(){
            return token.value();
        }
    }

    /**
     * Идентификатор
     *
     * @param token
     * @param <S>
     */
    record IdentAst<S extends CharPointer<S>>(IdentifierToken<S> token) implements Ast<S>,
                                                                                   Primitive,
                                                                                   Key<S> {
        public static IdentAst<DummyCharPointer> create(String value) {
            if (value == null) throw new IllegalArgumentException("value==null");
            return new IdentAst<>(new IdentifierToken<>(value, DummyCharPointer.instance, DummyCharPointer.instance));
        }

        public String value(){
            return token.value();
        }
    }

    sealed interface Key<S extends CharPointer<S>> extends Ast<S> {
        String value();
    }

    /**
     * Пара ключ-значение
     *
     * @param key   ключ
     * @param value значение
     * @param <S>
     */
    record KeyValue<S extends CharPointer<S>>(Key<S> key, Ast<S> value) implements Ast<S> {
        public static KeyValue<DummyCharPointer> create(Key<DummyCharPointer> key, Ast<DummyCharPointer> value) {
            if (key == null) throw new IllegalArgumentException("key==null");
            if (value == null) throw new IllegalArgumentException("value==null");
            return new KeyValue<>(key, value);
        }
    }

    /**
     * Объект
     *
     * @param values пары ключ-значение
     * @param begin  начало объекта
     * @param end    конец объекта
     * @param <S>
     */
    record ObjectAst<S extends CharPointer<S>>(
        ImList<KeyValue<S>> values,
        OpenParentheses<S> begin,
        CloseParentheses<S> end
    ) implements Ast<S> {
        public static ObjectAst<DummyCharPointer> create(ImList<KeyValue<DummyCharPointer>> values) {
            if (values == null) throw new IllegalArgumentException("values==null");
            return new ObjectAst<>(
                values,
                new OpenParentheses<>(DummyCharPointer.instance, DummyCharPointer.instance),
                new CloseParentheses<>(DummyCharPointer.instance, DummyCharPointer.instance)
            );
        }

        public Optional<Ast<S>> get(String key){
            if( key==null ) throw new IllegalArgumentException("key==null");
            for( var kv : values ){
                if( kv.key() instanceof Ast.StringAst<S> str ){
                    if(key.equals(str.value())){
                        return Optional.of(kv.value());
                    }
                }else if( kv.key() instanceof Ast.IdentAst<S> idt){
                    if( key.equals(idt.value()) ){
                        return Optional.of(kv.value());
                    }
                }
            }
            return Optional.empty();
        }

        public ImList<String> stringKeys(){
            return values().fmap(Ast.StringAst.class).map(StringAst::value);
        }

        public Ast<S> put(StringToken<S> key, Ast<S> value){
            if( key==null ) throw new IllegalArgumentException("key==null");
            if( value==null ) throw new IllegalArgumentException("value==null");
            var vals = values.filter( kv -> !key.value().equals(kv.key().value()) ).prepend( new KeyValue<>(new StringAst<>(key),value) );
            return new ObjectAst<>( vals, begin(), end() );
        }

        public Ast<S> put(Key<S> key, Ast<S> value) {
            if( key==null ) throw new IllegalArgumentException("key==null");
            if( value==null ) throw new IllegalArgumentException("value==null");
            var vals = values.filter( kv -> !key.value().equals(kv.key().value()) ).prepend( new KeyValue<>(key,value) );
            return new ObjectAst<>( vals, begin(), end() );
        }
    }

    /**
     * Массив
     *
     * @param values значения
     * @param begin  начало массива
     * @param end    конец массива
     * @param <S>
     */
    record ArrayAst<S extends CharPointer<S>>(
        ImList<Ast<S>> values, OpenSquare<S> begin, CloseSquare<S> end) implements Ast<S> {
        public static ArrayAst<DummyCharPointer> create(ImList<Ast<DummyCharPointer>> values) {
            if (values == null) throw new IllegalArgumentException("values==null");
            return new ArrayAst<>(
                values,
                new OpenSquare<>(DummyCharPointer.instance, DummyCharPointer.instance),
                new CloseSquare<>(DummyCharPointer.instance, DummyCharPointer.instance)
            );
        }
    }
}
