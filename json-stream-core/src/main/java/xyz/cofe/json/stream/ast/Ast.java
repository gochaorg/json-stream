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
import xyz.cofe.json.stream.token.StringPointer;
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
    default String toJson(AstWriter writer) {
        if (writer == null) throw new IllegalArgumentException("writer==null");
        return AstWriter.toString(this);
    }

    default String toJson(boolean pretty) {
        return AstWriter.toString(this, pretty);
    }

    default String toJson() {
        return toJson(false);
    }

    public static Ast<StringPointer> parse(String source) {
        if (source == null) throw new IllegalArgumentException("source==null");
        return AstParser.parse(source);
    }

    /**
     * Расположение начала в исходниках
     *
     * @return Расположение в исходниках
     */
    S sourceBegin();

    /**
     * Расположение конца в исходниках
     *
     * @return Расположение в исходниках
     */
    S sourceEnd();

    /**
     * Попытка преобразовать к числу
     *
     * @return число
     */
    default Optional<Number> asNumber() {
        if (this instanceof NumberAst.IntAst<?> a) return Optional.of(a.value());
        if (this instanceof NumberAst.DoubleAst<?> a) return Optional.of(a.value());
        if (this instanceof NumberAst.LongAst<?> a) return Optional.of(a.value());
        if (this instanceof NumberAst.BigIntAst<?> a) return Optional.of(a.value());
        return Optional.empty();
    }

    /**
     * Попытка преобразовать к строке из {@link StringAst}
     *
     * @return строка
     */
    default Optional<String> asString() {
        if (this instanceof StringAst<?> s) return Optional.of(s.value());
        return Optional.empty();
    }

    /**
     * Попытка преобразовать к числу
     *
     * @return число
     */
    default Optional<Integer> asInt() {
        if (this instanceof NumberAst.IntAst<?> a) return Optional.of(a.value());
        return Optional.empty();
    }

    /**
     * Попытка преобразовать к числу
     *
     * @return число
     */
    default Optional<Double> asDouble() {
        if (this instanceof NumberAst.DoubleAst<?> a) return Optional.of(a.value());
        return Optional.empty();
    }

    /**
     * Попытка преобразовать к числу
     *
     * @return число
     */
    default Optional<Long> asLong() {
        if (this instanceof NumberAst.LongAst<?> a) return Optional.of(a.value());
        return Optional.empty();
    }

    /**
     * Попытка преобразовать к числу
     *
     * @return число
     */
    default Optional<BigInteger> asBigInt() {
        if (this instanceof NumberAst.BigIntAst<?> a) return Optional.of(a.value());
        return Optional.empty();
    }

    /**
     * Попытка преобразовать к null
     *
     * @return null значение
     */
    default Optional<Result.NoValue> asNull() {
        if (this instanceof Ast.NullAst<?> a) return Optional.of(Result.NoValue.instance);
        return Optional.empty();
    }

    /**
     * Попытка преобразовать к строке из {@link IdentAst}
     *
     * @return строка
     */
    default Optional<String> asIdent() {
        if (this instanceof Ast.IdentAst<?> a) return Optional.of(a.value());
        return Optional.empty();
    }

    /**
     * Попытка преобразовать к строке из {@link StringAst} или {@link IdentAst}
     *
     * @return строка
     */
    default Optional<String> asText() {
        return asString().or(this::asIdent);
    }

    /**
     * Попытка преобразование к boolean
     *
     * @return значение
     */
    default Optional<Boolean> asBoolean() {
        if (this instanceof Ast.BooleanAst<?> a) return Optional.of(a.value());
        return Optional.empty();
    }

    /**
     * Попытка преобразования к списку
     *
     * @return список
     */
    default Optional<ImList<Ast<S>>> asList() {
        if (this instanceof Ast.ArrayAst<S> a) return Optional.of(a.values());
        return Optional.empty();
    }

    /**
     * Попытка преобразования к объекту
     *
     * @return знание - объект
     */
    default Optional<Ast.ObjectAst<S>> asObject() {
        if (this instanceof Ast.ObjectAst<S> a) return Optional.of(a);
        return Optional.empty();
    }

    /**
     * Комментарий
     *
     * @param <S> Тип исходника
     */
    sealed interface Comment<S extends CharPointer<S>> extends Ast<S> {
        /**
         * Многострочный комментарий
         *
         * @param token Лексема
         * @param <S>   Тип исходника
         */
        record MultiLine<S extends CharPointer<S>>(MLComment<S> token) implements Comment<S> {
            public MultiLine {
                if (token == null) throw new IllegalArgumentException("token==null");
            }

            @Override
            public S sourceBegin() {
                return token.begin();
            }

            @Override
            public S sourceEnd() {
                return token.end();
            }

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
         * @param token Лексема
         * @param <S>   Тип исходника
         */
        record SingleLine<S extends CharPointer<S>>(SLComment<S> token) implements Comment<S> {
            public SingleLine {
                if (token == null) throw new IllegalArgumentException("token==null");
            }

            @Override
            public S sourceBegin() {
                return token.begin();
            }

            @Override
            public S sourceEnd() {
                return token.end();
            }

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
     * @param <S> Тип исходника
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
            public DoubleAst {
                if (token == null) throw new IllegalArgumentException("token==null");
            }

            @Override
            public S sourceBegin() {
                return token.begin();
            }

            @Override
            public S sourceEnd() {
                return token.end();
            }

            public static DoubleAst<DummyCharPointer> create(double value) {
                return new DoubleAst<>(new DoubleToken<>(value, DummyCharPointer.instance, DummyCharPointer.instance));
            }

            public static <S1 extends CharPointer<S1>> DoubleAst<S1> create(double value, S1 begin, S1 end) {
                if (begin == null) throw new IllegalArgumentException("begin==null");
                if (end == null) throw new IllegalArgumentException("end==null");
                return new DoubleAst<>(new DoubleToken<>(value, begin, end));
            }

            public double value() {
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
            public IntAst {
                if (token == null) throw new IllegalArgumentException("token==null");
            }

            @Override
            public S sourceBegin() {
                return token.begin();
            }

            @Override
            public S sourceEnd() {
                return token.end();
            }

            public static IntAst<DummyCharPointer> create(int value) {
                return new IntAst<>(new IntToken<>(value, DummyCharPointer.instance, DummyCharPointer.instance));
            }

            public static <S1 extends CharPointer<S1>> IntAst<S1> create(int value, S1 begin, S1 end) {
                if (begin == null) throw new IllegalArgumentException("begin==null");
                if (end == null) throw new IllegalArgumentException("end==null");
                return new IntAst<>(new IntToken<>(value, begin, end));
            }

            public int value() {
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
            public LongAst {
                if (token == null) throw new IllegalArgumentException("token==null");
            }

            @Override
            public S sourceBegin() {
                return token.begin();
            }

            @Override
            public S sourceEnd() {
                return token.end();
            }

            public static LongAst<DummyCharPointer> create(long value) {
                return new LongAst<>(new LongToken<>(value, DummyCharPointer.instance, DummyCharPointer.instance));
            }

            public static <S1 extends CharPointer<S1>> LongAst<S1> create(long value, S1 begin, S1 end) {
                if (begin == null) throw new IllegalArgumentException("begin==null");
                if (end == null) throw new IllegalArgumentException("end==null");
                return new LongAst<>(new LongToken<>(value, begin, end));
            }

            public long value() {
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
            public BigIntAst {
                if (token == null) throw new IllegalArgumentException("token==null");
            }

            @Override
            public S sourceBegin() {
                return token.begin();
            }

            @Override
            public S sourceEnd() {
                return token.end();
            }

            public static BigIntAst<DummyCharPointer> create(BigInteger value) {
                if (value == null) throw new IllegalArgumentException("value==null");
                return new BigIntAst<>(new BigIntToken<>(value, DummyCharPointer.instance, DummyCharPointer.instance));
            }

            public static <S1 extends CharPointer<S1>> BigIntAst<S1> create(BigInteger value, S1 begin, S1 end) {
                if (value == null) throw new IllegalArgumentException("value==null");
                if (begin == null) throw new IllegalArgumentException("begin==null");
                if (end == null) throw new IllegalArgumentException("end==null");
                return new BigIntAst<>(new BigIntToken<>(value, begin, end));
            }

            public BigInteger value() {
                return token().value();
            }
        }
    }

    /**
     * Булево значение
     *
     * @param <S> Тип исходника
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
            public TrueAst {
                if (token == null) throw new IllegalArgumentException("token==null");
            }

            @Override
            public S sourceBegin() {
                return token.begin();
            }

            @Override
            public S sourceEnd() {
                return token.end();
            }

            public static TrueAst<DummyCharPointer> create() {
                return new TrueAst<>(new TrueToken<>(DummyCharPointer.instance, DummyCharPointer.instance));
            }

            public static <S1 extends CharPointer<S1>> TrueAst<S1> create(S1 begin, S1 end) {
                if (begin == null) throw new IllegalArgumentException("begin==null");
                if (end == null) throw new IllegalArgumentException("end==null");

                return new TrueAst<>(new TrueToken<>(begin, end));
            }
        }

        /**
         * False значение
         *
         * @param token
         * @param <S>
         */
        record FalseAst<S extends CharPointer<S>>(FalseToken<S> token) implements BooleanAst<S> {
            public FalseAst {
                if (token == null) throw new IllegalArgumentException("token==null");
            }

            @Override
            public S sourceBegin() {
                return token.begin();
            }

            @Override
            public S sourceEnd() {
                return token.end();
            }

            public static FalseAst<DummyCharPointer> create() {
                return new FalseAst<>(new FalseToken<>(DummyCharPointer.instance, DummyCharPointer.instance));
            }

            public static <S1 extends CharPointer<S1>> FalseAst<S1> create(S1 begin, S1 end) {
                if (begin == null) throw new IllegalArgumentException("begin==null");
                if (end == null) throw new IllegalArgumentException("end==null");

                return new FalseAst<>(new FalseToken<>(begin, end));
            }
        }

        public static BooleanAst<DummyCharPointer> create(boolean value) {
            return value ? TrueAst.create() : FalseAst.create();
        }

        public static <S1 extends CharPointer<S1>> BooleanAst<S1> create(boolean value, S1 begin, S1 end) {
            return value ? TrueAst.create(begin, end) : FalseAst.create(begin, end);
        }

        default public boolean value() {
            return this instanceof BooleanAst.TrueAst<S>;
        }
    }

    /**
     * Null значение
     *
     * @param token Лексема
     * @param <S>   Тип исходника
     */
    record NullAst<S extends CharPointer<S>>(NullToken<S> token) implements Ast<S>,
                                                                            Primitive {
        public NullAst {
            if (token == null) throw new IllegalArgumentException("token==null");
        }

        @Override
        public S sourceBegin() {
            return token.begin();
        }

        @Override
        public S sourceEnd() {
            return token.end();
        }

        /**
         * Конструктор
         *
         * @return значение
         */
        public static NullAst<DummyCharPointer> create() {
            return new NullAst<DummyCharPointer>(new NullToken<>(DummyCharPointer.instance, DummyCharPointer.instance));
        }

        /**
         * Конструктор
         *
         * @return значение
         */
        public static <S1 extends CharPointer<S1>> NullAst<S1> create(S1 begin, S1 end) {
            if (begin == null) throw new IllegalArgumentException("begin==null");
            if (end == null) throw new IllegalArgumentException("end==null");
            return new NullAst<>(new NullToken<>(begin, end));
        }
    }

    /**
     * Строка
     *
     * @param token лексема
     * @param <S>   Тип исходника
     */
    record StringAst<S extends CharPointer<S>>(StringToken<S> token) implements Ast<S>,
                                                                                Primitive,
                                                                                Key<S> {
        public StringAst {
            if (token == null) throw new IllegalArgumentException("token==null");
        }

        @Override
        public S sourceBegin() {
            return token.begin();
        }

        @Override
        public S sourceEnd() {
            return token.end();
        }

        /**
         * Конструктор строка
         *
         * @param value значение
         * @return строка
         */
        public static StringAst<DummyCharPointer> create(String value) {
            if (value == null) throw new IllegalArgumentException("value==null");
            return new StringAst<>(new StringToken<>(value, DummyCharPointer.instance, DummyCharPointer.instance));
        }


        /**
         * Конструктор строка
         *
         * @param value значение
         * @return строка
         */
        public static <S1 extends CharPointer<S1>> StringAst<S1> create(String value, S1 begin, S1 end) {
            if (value == null) throw new IllegalArgumentException("value==null");
            if (begin == null) throw new IllegalArgumentException("begin==null");
            if (end == null) throw new IllegalArgumentException("end==null");
            return new StringAst<>(new StringToken<>(value, begin, end));
        }

        /**
         * Декодированное значение
         *
         * @return значение
         */
        public String value() {
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
        public IdentAst {
            if (token == null) throw new IllegalArgumentException("token==null");
        }

        @Override
        public S sourceBegin() {
            return token.begin();
        }

        @Override
        public S sourceEnd() {
            return token.end();
        }

        public static IdentAst<DummyCharPointer> create(String value) {
            if (value == null) throw new IllegalArgumentException("value==null");
            return new IdentAst<>(new IdentifierToken<>(value, DummyCharPointer.instance, DummyCharPointer.instance));
        }

        public static <S1 extends CharPointer<S1>> IdentAst<S1> create(String value, S1 begin, S1 end) {
            if (value == null) throw new IllegalArgumentException("value==null");
            if (begin == null) throw new IllegalArgumentException("begin==null");
            if (end == null) throw new IllegalArgumentException("end==null");
            return new IdentAst<>(new IdentifierToken<>(value, begin, end));
        }

        public String value() {
            return token.value();
        }
    }

    /**
     * Ключ в {@link ObjectAst}
     *
     * @param <S> Тип исходника
     */
    sealed interface Key<S extends CharPointer<S>> extends Ast<S> {
        /**
         * Значение ключа
         *
         * @return значение
         */
        String value();
    }

    /**
     * Пара ключ-значение
     *
     * @param key   ключ
     * @param value значение
     * @param <S>   тип исходника
     */
    record KeyValue<S extends CharPointer<S>>(Key<S> key, Ast<S> value) implements Ast<S> {
        public KeyValue {
            if (key == null) throw new IllegalArgumentException("key==null");
            if (value == null) throw new IllegalArgumentException("value==null");
        }

        @Override
        public S sourceBegin() {
            return key.sourceBegin();
        }

        @Override
        public S sourceEnd() {
            return value.sourceEnd();
        }

        public static <S1 extends CharPointer<S1>> KeyValue<S1> create(Key<S1> key, Ast<S1> value) {
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
     * @param <S>    тип исходника
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

        public static <S1 extends CharPointer<S1>> ObjectAst<S1> create(ImList<KeyValue<S1>> values, OpenParentheses<S1> open, CloseParentheses<S1> close) {
            if (values == null) throw new IllegalArgumentException("values==null");
            if (open == null) throw new IllegalArgumentException("open==null");
            if (close == null) throw new IllegalArgumentException("close==null");
            return new ObjectAst<>(
                values,
                open,
                close
            );
        }

        public static <S1 extends CharPointer<S1>> ObjectAst<S1> create(ImList<KeyValue<S1>> values, S1 openBegin, S1 openEnd, S1 closeBegin, S1 closeEnd) {
            if (values == null) throw new IllegalArgumentException("values==null");

            if (openBegin == null) throw new IllegalArgumentException("openBegin==null");
            if (openEnd == null) throw new IllegalArgumentException("openEnd==null");

            if (closeBegin == null) throw new IllegalArgumentException("closeBegin==null");
            if (closeEnd == null) throw new IllegalArgumentException("closeEnd==null");

            return new ObjectAst<>(
                values,
                new OpenParentheses<>(openBegin, openEnd),
                new CloseParentheses<>(closeBegin, closeEnd)
            );
        }

        public ObjectAst {
            if (values == null) throw new IllegalArgumentException("values==null");
            if (begin == null) throw new IllegalArgumentException("begin==null");
            if (end == null) throw new IllegalArgumentException("end==null");
        }

        @Override
        public S sourceBegin() {
            return begin.begin();
        }

        @Override
        public S sourceEnd() {
            return end.end();
        }

        /**
         * Значение по указанному ключу
         *
         * @param key ключ
         * @return значение
         */
        public Optional<Ast<S>> get(String key) {
            if (key == null) throw new IllegalArgumentException("key==null");
            for (var kv : values) {
                if (kv.key() instanceof Ast.StringAst<S> str) {
                    if (key.equals(str.value())) {
                        return Optional.of(kv.value());
                    }
                } else if (kv.key() instanceof Ast.IdentAst<S> idt) {
                    if (key.equals(idt.value())) {
                        return Optional.of(kv.value());
                    }
                }
            }
            return Optional.empty();
        }

        /**
         * Клонирует и добавляет еще одну пару ключ-значение
         *
         * @param key   ключ
         * @param value значение
         * @return клон с добавленной парой
         */
        public ObjectAst<S> put(StringToken<S> key, Ast<S> value) {
            if (key == null) throw new IllegalArgumentException("key==null");
            if (value == null) throw new IllegalArgumentException("value==null");
            var vals = values.filter(kv -> !key.value().equals(kv.key().value())).prepend(new KeyValue<>(new StringAst<>(key), value));
            return new ObjectAst<>(vals, begin(), end());
        }

        /**
         * Клонирует и добавляет еще одну пару ключ-значение
         *
         * @param key   ключ
         * @param value значение
         * @return клон с добавленной парой
         */
        public ObjectAst<S> put(Key<S> key, Ast<S> value) {
            if (key == null) throw new IllegalArgumentException("key==null");
            if (value == null) throw new IllegalArgumentException("value==null");
            var vals = values.filter(kv -> !key.value().equals(kv.key().value())).prepend(new KeyValue<>(key, value));
            return new ObjectAst<>(vals, begin(), end());
        }
    }

    /**
     * Массив
     *
     * @param values значения
     * @param begin  начало массива
     * @param end    конец массива
     * @param <S>тип исходника
     */
    record ArrayAst<S extends CharPointer<S>>(
        ImList<Ast<S>> values, OpenSquare<S> begin, CloseSquare<S> end) implements Ast<S> {

        public ArrayAst {
            if (values == null) throw new IllegalArgumentException("values==null");
            if (begin == null) throw new IllegalArgumentException("begin==null");
            if (end == null) throw new IllegalArgumentException("end==null");
        }

        @Override
        public S sourceBegin() {
            return begin.begin();
        }

        @Override
        public S sourceEnd() {
            return end.end();
        }

        /**
         * Создание/конструктор
         *
         * @param values значения
         * @return значение - массив
         */
        public static ArrayAst<DummyCharPointer> create(ImList<Ast<DummyCharPointer>> values) {
            if (values == null) throw new IllegalArgumentException("values==null");
            return new ArrayAst<>(
                values,
                new OpenSquare<>(DummyCharPointer.instance, DummyCharPointer.instance),
                new CloseSquare<>(DummyCharPointer.instance, DummyCharPointer.instance)
            );
        }

        public static <S1 extends CharPointer<S1>> ArrayAst<S1> create(ImList<Ast<S1>> values, OpenSquare<S1> openSquare, CloseSquare<S1> closeSquare) {
            if (values == null) throw new IllegalArgumentException("values==null");
            if (openSquare == null) throw new IllegalArgumentException("openSquare==null");
            if (closeSquare == null) throw new IllegalArgumentException("closeSquare==null");
            return new ArrayAst<>(
                values,
                openSquare,
                closeSquare
            );
        }

        public static <S1 extends CharPointer<S1>> ArrayAst<S1> create(ImList<Ast<S1>> values, S1 openSquareBegin, S1 openSquareEnd, S1 closeSquareBegin, S1 closeSquareEnd) {
            if (values == null) throw new IllegalArgumentException("values==null");
            if (openSquareBegin == null) throw new IllegalArgumentException("openSquareBegin==null");
            if (openSquareEnd == null) throw new IllegalArgumentException("openSquareEnd==null");
            if (closeSquareBegin == null) throw new IllegalArgumentException("closeSquareBegin==null");
            if (closeSquareEnd == null) throw new IllegalArgumentException("closeSquareEnd==null");

            return new ArrayAst<>(
                values,
                new OpenSquare<>(openSquareBegin, openSquareEnd),
                new CloseSquare<>(closeSquareBegin, closeSquareEnd)
            );
        }
    }
}
