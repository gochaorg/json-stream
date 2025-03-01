package xyz.cofe.json.stream.ast;

import jdk.security.jarsigner.JarSignerException;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.token.BigIntToken;
import xyz.cofe.json.stream.token.CharPointer;
import xyz.cofe.json.stream.token.CloseParentheses;
import xyz.cofe.json.stream.token.CloseSquare;
import xyz.cofe.json.stream.token.Colon;
import xyz.cofe.json.stream.token.Comma;
import xyz.cofe.json.stream.token.DoubleToken;
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
import xyz.cofe.json.stream.token.Token;
import xyz.cofe.json.stream.token.Tokenizer;
import xyz.cofe.json.stream.token.TrueToken;
import xyz.cofe.json.stream.token.Whitespace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Парсер JSON
 *
 * <p>Пример использования</p>
 *
 * <pre>
 * var ast = AstParser.parse("""
 * {
 *     "a": 1,
 *     b: 2,
 *     c: [ 4, 5 ]
 * }
 * """ );
 * </pre>
 *
 * <p>Парсинг из потока лексем</p>
 *
 * <pre>
 * // Исходник, может быть строкой, файлом или сокетом
 * var source = "[ 1, true, false, null, [], [ 'abc', 2.5 ] ]";
 *
 * // Лексический анализатор может выдавать последовательно лексемы
 * var parsed = Tokenizer.parse(source);
 *
 * // Тут будет получен результат
 * Ast&lt;StringPointer&gt; last = null;
 *
 * // Создаем парсер
 * AstParser&lt;StringPointer&gt; parser = new AstParser.Init&lt;&gt;();
 *
 * // Последовательно скармливаем лексемы в парсер
 * for (var token : parsed.tokens()) {
 *
 *     // Очередная лексема
 *     System.out.println("token " + token);
 *
 *     // Скармливаем ее
 *     var astParsed = parser.input(token);
 *
 *     // Переключаем парсер на новое состояние
 *     parser = astParsed.parser();
 *
 *     // Если есть результат выводим его
 *     astParsed.result().ifPresent(ast -&gt; {
 *
 *         // Тут будет распознаное значение json
 *         System.out.println("result " + ast);
 *     });
 *
 *     // Получаем последнее значение
 *     last = astParsed.result().orElse(null);
 * }
 * </pre>
 *
 * <p>Таблица переходов состояния</p>
 *
 * <pre>
 *                    | init   | obj   | obj.k | obj.v | arr   | arr.c
 * -------------------|--------|-------|-------|-------|-------|------
 * BigIntToken        | init   | err   | err   | obj.c | arr.c | err
 * IntToken           | init   | err   | err   | obj.c | arr.c | err
 * LongToken          | init   | err   | err   | obj.c | arr.c | err
 * DoubleToken        | init   | err   | err   | obj.c | arr.c | err
 * StringToken        | init   | obj.k | err   | obj.c | arr.c | err
 * FalseToken         | init   | err   | err   | obj.c | arr.c | err
 * TrueToken          | init   | err   | err   | obj.c | arr.c | err
 * NullToken          | init   | err   | err   | obj.c | arr.c | err
 * IdentifierToken    | init   | obj.k | err   | obj.c | arr.c | err
 * OpenParentheses  { | obj    | err   | err   | obj   | obj   | err
 * CloseParentheses } | err    | err   | err   | init  | err   | err
 * OpenSquare       [ | arr    | err   | err   | arr   | arr   | err
 * CloseSquare      ] | err    | err   | err   | err   | init  | init
 * Colon            : | err    | err   | obj.v | err   | err   | err
 * Comma            , | err    | err   | err   | obj   | err   | arr
 * MLComment          | init   | obj   | obj.k | obj.v | arr   | arr.c
 * SLComment          | init   | obj   | obj.k | obj.v | arr   | arr.c
 * Whitespace         | init   | obj   | obj.k | obj.v | arr   | arr.c
 * </pre>
 */
public sealed interface AstParser<S extends CharPointer<S>> {
    default int getNestedLevel() {
        int level = 0;
        AstParser parser = this;
        while (true) {
            if (parser instanceof AstParser.ObjectParser<?> op) {
                parser = op.getParent();
                level++;
            } else if (parser instanceof AstParser.ArrayParser<?> ap) {
                parser = ap.getParent();
                level++;
            } else {
                break;
            }
        }
        return level;
    }

    record ParserOptions(
        boolean identAtRoot,
        boolean identInObjectKey,
        boolean identInObjectValue,
        boolean identInArrayValue,
        boolean returnNestedValue,
        boolean singleLineComment,
        boolean multiLineComment
    ) {
        public ParserOptions() {
            this(
                false,
                true,
                false,
                false,
                false,
                true,
                true
            );
        }
    }

    record Parsed<S extends CharPointer<S>>(AstParser<S> parser, Optional<Ast<S>> result) {}

    Parsed<S> input(Token<S> token);

    private static <S extends CharPointer<S>> Parsed<S> ok(AstParser<S> parser, Ast<S> result) {
        return new Parsed<>(parser, Optional.of(result));
    }

    private static <S extends CharPointer<S>> Parsed<S> ok(AstParser<S> parser) {
        return new Parsed<>(parser, Optional.empty());
    }

    private static <S extends CharPointer<S>> Parsed<S> err(String message) {
        return new Parsed<>(new Error<S>(message), Optional.empty());
    }

    @SuppressWarnings("rawtypes")
    @SafeVarargs
    private static <S extends CharPointer<S>> Parsed<S> unexpectedToken(Token<S> token, Class<Token>... expect) {
        return err("unexpected lexem " + token + " at " + token.begin() + ", was expect the following lexemes " + Arrays.toString(expect));
    }

    public static final class Init<S extends CharPointer<S>>
        implements AstParser<S> {
        private final ParserOptions options;

        public Init(ParserOptions options) {
            if (options == null) throw new IllegalArgumentException("options==null");
            this.options = options;
        }

        public Init() {
            this(new ParserOptions());
        }

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        private final List<Ast.Comment<S>> comments = new ArrayList<>();

        private List<Class<?>> startTokens() {
            List<Class<?>> tokens = new ArrayList<>();
            tokens.add(BigIntToken.class);
            tokens.add(LongToken.class);
            tokens.add(IntToken.class);
            tokens.add(DoubleToken.class);
            if (options.identAtRoot()) tokens.add(IdentifierToken.class);
            tokens.add(StringToken.class);
            tokens.add(FalseToken.class);
            tokens.add(TrueToken.class);
            tokens.add(NullToken.class);
            if (options.singleLineComment) tokens.add(SLComment.class);
            if (options.multiLineComment) tokens.add(MLComment.class);
            tokens.add(OpenParentheses.class);
            tokens.add(OpenSquare.class);
            tokens.add(Whitespace.class);
            return tokens;
        }

        private Parsed<S> unexpectedStartToken(Token<S> token) {
            //noinspection unchecked
            return unexpectedToken(token, startTokens().toArray(new Class[0]));
        }

        @Override
        public Parsed<S> input(Token<S> token) {
            if (token == null) throw new IllegalArgumentException("token==null");

            if (token instanceof BigIntToken<S> n) return ok(this, new Ast.NumberAst.BigIntAst<>(n));
            if (token instanceof LongToken<S> n) return ok(this, new Ast.NumberAst.LongAst<>(n));
            if (token instanceof IntToken<S> n) return ok(this, new Ast.NumberAst.IntAst<>(n));
            if (token instanceof DoubleToken<S> n) return ok(this, new Ast.NumberAst.DoubleAst<>(n));
            if (token instanceof IdentifierToken<S> n) {
                if (!options.identAtRoot()) return unexpectedStartToken(token);
                return ok(this, new Ast.IdentAst<>(n));
            }
            if (token instanceof StringToken<S> n) return ok(this, new Ast.StringAst<>(n));
            if (token instanceof FalseToken<S> n) return ok(this, new Ast.BooleanAst.FalseAst<>(n));
            if (token instanceof TrueToken<S> n) return ok(this, new Ast.BooleanAst.TrueAst<>(n));
            if (token instanceof NullToken<S> n) return ok(this, new Ast.NumberAst.NullAst<>(n));
            if (token instanceof Whitespace<S> n) return ok(this);
            if (token instanceof SLComment<S> n) {
                if (!options.singleLineComment) return unexpectedStartToken(token);
                comments.add(new Ast.Comment.SingleLine<>(n));
                return ok(this);
            }
            if (token instanceof MLComment<S> n) {
                if (!options.multiLineComment) return unexpectedStartToken(token);
                comments.add(new Ast.Comment.MultiLine<>(n));
                return ok(this);
            }
            if (token instanceof Comma<S> n) return unexpectedStartToken(token);
            if (token instanceof Colon<S> n) return unexpectedStartToken(token);
            if (token instanceof CloseParentheses<S> n) return unexpectedStartToken(token);
            if (token instanceof CloseSquare<S> n) return unexpectedStartToken(token);
            if (token instanceof OpenSquare<S> n) return ok(new ArrayParser<>(options, this, n));
            if (token instanceof OpenParentheses<S> n) return ok(new ObjectParser<>(options, this, n));
            return unexpectedStartToken(token);
        }
    }

    /**
     * Ошибка парснга
     *
     * @param message сообщение
     * @param <S>     тип источника
     */
    public record Error<S extends CharPointer<S>>(String message) implements AstParser<S> {
        @Override
        public Parsed<S> input(Token<S> token) {
            return ok(this);
        }
    }

    /**
     * Парсинг вложенного объекта
     *
     * @param <S>
     */
    public static final class ObjectParser<S extends CharPointer<S>> implements AstParser<S> {
        private final ParserOptions options;
        private final AstParser<S> parent;
        private final OpenParentheses<S> begin;
        private final Consumer<Ast<S>> resultConsumer;
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        private final List<Ast.Comment<S>> comments = new ArrayList<>();

        public enum State {
            ExpectKey,
            AfterKey,
            ExpectValue,
            AfterValue
        }

        private final List<Ast.KeyValue<S>> values = new ArrayList<>();
        private Ast.Key<S> key;

        public Ast.Key<?> getKey() {
            return key;
        }

        private State state = State.ExpectKey;

        public State getState() {
            return state;
        }

        public AstParser<S> getParent() {return parent;}

        public ObjectParser(ParserOptions options, AstParser<S> parent, OpenParentheses<S> begin) {
            this.parent = parent;
            this.begin = begin;
            this.options = options;
            resultConsumer = ignore -> {};
        }

        public ObjectParser(ParserOptions options, AstParser<S> parent, OpenParentheses<S> begin, Consumer<Ast<S>> resultConsumer) {
            this.parent = parent;
            this.begin = begin;
            this.options = options;
            this.resultConsumer = resultConsumer;
        }

        private List<Class<?>> keyTokens() {
            List<Class<?>> tokens = new ArrayList<>();
            if (options.identInObjectKey) tokens.add(IdentifierToken.class);
            tokens.add(StringToken.class);
            tokens.add(SLComment.class);
            tokens.add(MLComment.class);
            tokens.add(Whitespace.class);
            tokens.add(CloseParentheses.class);
            return tokens;
        }

        @SuppressWarnings("unchecked")
        private Parsed<S> unexpectedKeyToken(Token<S> token) {
            return unexpectedToken(token, keyTokens().toArray(new Class[0]));
        }

        private List<Class<?>> afterKeyTokens() {
            List<Class<?>> tokens = new ArrayList<>();
            tokens.add(Colon.class);
            if (options.singleLineComment) tokens.add(SLComment.class);
            if (options.multiLineComment) tokens.add(MLComment.class);
            tokens.add(Whitespace.class);
            return tokens;
        }

        @SuppressWarnings("unchecked")
        private Parsed<S> unexpectedAfterKeyToken(Token<S> token) {
            return unexpectedToken(token, afterKeyTokens().toArray(new Class[0]));
        }

        private List<Class<?>> valueTokens() {
            List<Class<?>> tokens = new ArrayList<>();
            tokens.add(BigIntToken.class);
            tokens.add(LongToken.class);
            tokens.add(IntToken.class);
            tokens.add(DoubleToken.class);
            if (options.identInObjectValue) tokens.add(IdentifierToken.class);
            tokens.add(StringToken.class);
            tokens.add(FalseToken.class);
            tokens.add(TrueToken.class);
            tokens.add(NullToken.class);
            if (options.singleLineComment) tokens.add(SLComment.class);
            if (options.multiLineComment) tokens.add(MLComment.class);
            tokens.add(OpenParentheses.class);
            tokens.add(OpenSquare.class);
            tokens.add(Whitespace.class);
            return tokens;
        }

        @SuppressWarnings("unchecked")
        private Parsed<S> unexpectedValueToken(Token<S> token) {
            return unexpectedToken(token, valueTokens().toArray(new Class[0]));
        }

        @SuppressWarnings("unchecked")
        private Parsed<S> unexpectedAfterValueToken(Token<S> token) {
            return unexpectedToken(token, valueTokens().toArray(new Class[0]));
        }

        @Override
        public Parsed<S> input(Token<S> token) {
            if (token == null) throw new IllegalArgumentException("token==null");
            switch (state) {
                case ExpectKey -> {
                    if (token instanceof StringToken<S> t) {
                        key = new Ast.StringAst<S>(t);
                        state = State.AfterKey;
                        return ok(this);
                    } else if (token instanceof IdentifierToken<S> t) {
                        if (!options.identInObjectKey)
                            return unexpectedKeyToken(t);

                        key = new Ast.IdentAst<S>(t);
                        state = State.AfterKey;
                        return ok(this);
                    } else if (token instanceof MLComment<S> t) {
                        if (!options.multiLineComment) return unexpectedKeyToken(t);
                        comments.add(new Ast.Comment.MultiLine<>(t));
                        return ok(this);
                    } else if (token instanceof SLComment<S> t) {
                        if (!options.singleLineComment) return unexpectedKeyToken(t);
                        comments.add(new Ast.Comment.SingleLine<>(t));
                        return ok(this);
                    } else if (token instanceof Whitespace<S> t) {
                        return ok(this);
                    } else if (token instanceof CloseParentheses<S> t) {
                        var res = new Ast.ObjectAst<>(
                            ImList.from(values),
                            begin,
                            t
                        );
                        resultConsumer.accept(res);

                        boolean nested = !(parent instanceof AstParser.Init<S>);
                        if (nested) {
                            if (options.returnNestedValue) {
                                return ok(parent, res);
                            } else {
                                return ok(parent);
                            }
                        } else {
                            return ok(parent, res);
                        }
                    } else {
                        return unexpectedKeyToken(token);
                    }
                }
                case AfterKey -> {
                    if (token instanceof Colon<S> t) {
                        state = State.ExpectValue;
                        return ok(this);
                    } else if (token instanceof MLComment<S> t) {
                        if (!options.multiLineComment) return unexpectedAfterKeyToken(t);
                        comments.add(new Ast.Comment.MultiLine<>(t));
                        return ok(this);
                    } else if (token instanceof SLComment<S> t) {
                        if (!options.singleLineComment) return unexpectedAfterKeyToken(t);
                        comments.add(new Ast.Comment.SingleLine<>(t));
                        return ok(this);
                    } else if (token instanceof Whitespace<S> t) {
                        return ok(this);
                    } else {
                        return unexpectedAfterKeyToken(token);
                    }
                }
                case ExpectValue -> {
                    if (token instanceof BigIntToken<S> t) {
                        values.add(new Ast.KeyValue<>(key, new Ast.NumberAst.BigIntAst<>(t)));
                        state = State.AfterValue;
                        return ok(this);
                    } else if (token instanceof LongToken<S> t) {
                        values.add(new Ast.KeyValue<>(key, new Ast.NumberAst.LongAst<>(t)));
                        state = State.AfterValue;
                        return ok(this);
                    } else if (token instanceof IntToken<S> t) {
                        values.add(new Ast.KeyValue<>(key, new Ast.NumberAst.IntAst<>(t)));
                        state = State.AfterValue;
                        return ok(this);
                    } else if (token instanceof DoubleToken<S> t) {
                        values.add(new Ast.KeyValue<>(key, new Ast.NumberAst.DoubleAst<>(t)));
                        state = State.AfterValue;
                        return ok(this);
                    } else if (token instanceof StringToken<S> t) {
                        values.add(new Ast.KeyValue<>(key, new Ast.StringAst<>(t)));
                        state = State.AfterValue;
                        return ok(this);
                    } else if (token instanceof NullToken<S> t) {
                        values.add(new Ast.KeyValue<>(key, new Ast.NullAst<>(t)));
                        state = State.AfterValue;
                        return ok(this);
                    } else if (token instanceof TrueToken<S> t) {
                        values.add(new Ast.KeyValue<>(key, new Ast.BooleanAst.TrueAst<>(t)));
                        state = State.AfterValue;
                        return ok(this);
                    } else if (token instanceof FalseToken<S> t) {
                        values.add(new Ast.KeyValue<>(key, new Ast.BooleanAst.FalseAst<>(t)));
                        state = State.AfterValue;
                        return ok(this);
                    } else if (token instanceof IdentifierToken<S> t) {
                        if (!options.identInObjectValue)
                            return unexpectedValueToken(t);

                        values.add(new Ast.KeyValue<>(key, new Ast.IdentAst<>(t)));
                        state = State.AfterValue;
                        return ok(this);
                    } else if (token instanceof OpenParentheses<S> t) {
                        var kk = key;
                        var obj = new ObjectParser<>(options, this, t, res -> {
                            values.add(new Ast.KeyValue<>(kk, res));
                        });
                        state = State.AfterValue;
                        return ok(obj);
                    } else if (token instanceof OpenSquare<S> t) {
                        var kk = key;
                        var arr = new ArrayParser<>(options, this, t, res -> {
                            values.add(new Ast.KeyValue<>(kk, res));
                        });
                        state = State.AfterValue;
                        return ok(arr);
                    } else if (token instanceof MLComment<S> t) {
                        if (!options.multiLineComment) return unexpectedValueToken(t);
                        return ok(this);
                    } else if (token instanceof SLComment<S> t) {
                        if (!options.singleLineComment) return unexpectedValueToken(t);
                        comments.add(new Ast.Comment.SingleLine<>(t));
                        return ok(this);
                    } else if (token instanceof Whitespace<S> t) {
                        return ok(this);
                    } else {
                        return err("!");
                    }
                }
                case AfterValue -> {
                    if (token instanceof MLComment<S> t) {
                        if (!options.multiLineComment) return unexpectedAfterValueToken(t);
                        comments.add(new Ast.Comment.MultiLine<>(t));
                        return ok(this);
                    } else if (token instanceof SLComment<S> t) {
                        if (!options.singleLineComment) return unexpectedAfterValueToken(t);
                        comments.add(new Ast.Comment.SingleLine<>(t));
                        return ok(this);
                    } else if (token instanceof Whitespace<S> t) {
                        return ok(this);
                    } else if (token instanceof CloseParentheses<S> t) {
                        var res = new Ast.ObjectAst<>(
                            ImList.from(values),
                            begin,
                            t
                        );
                        resultConsumer.accept(res);

                        boolean nested = !(parent instanceof AstParser.Init<S>);
                        if (nested) {
                            if (options.returnNestedValue) {
                                return ok(parent, res);
                            } else {
                                return ok(parent);
                            }
                        } else {
                            return ok(parent, res);
                        }
                    } else if (token instanceof Comma<S> t) {
                        state = State.ExpectKey;
                        return ok(this);
                    } else {
                        return unexpectedAfterValueToken(token);
                    }
                }
            }
            return ok(this);
        }
    }

    /**
     * Парсинг вложенного массива
     *
     * @param <S>
     */
    public static final class ArrayParser<S extends CharPointer<S>> implements AstParser<S> {
        private final ParserOptions options;
        private final AstParser<S> parent;
        private final OpenSquare<S> begin;
        private final Consumer<Ast<S>> resultConsumer;

        private final List<Ast<S>> values = new ArrayList<>();

        public enum State {ExpectValue, AfterValue}
        private State state = State.ExpectValue;

        public State getState() {
            return state;
        }

        public AstParser<S> getParent() {
            return parent;
        }

        public ArrayParser(ParserOptions options, AstParser<S> parent, OpenSquare<S> begin) {
            this.begin = begin;
            this.parent = parent;
            this.resultConsumer = ignore -> {};
            this.options = options;
        }

        public ArrayParser(ParserOptions options, AstParser<S> parent, OpenSquare<S> begin, Consumer<Ast<S>> resultConsumer) {
            this.begin = begin;
            this.parent = parent;
            this.resultConsumer = resultConsumer;
            this.options = options;
        }

        private List<Class<?>> valueTokens() {
            List<Class<?>> tokens = new ArrayList<>();
            tokens.add(BigIntToken.class);
            tokens.add(LongToken.class);
            tokens.add(IntToken.class);
            tokens.add(DoubleToken.class);
            tokens.add(IdentifierToken.class);
            tokens.add(StringToken.class);
            tokens.add(FalseToken.class);
            tokens.add(TrueToken.class);
            tokens.add(NullToken.class);
            tokens.add(SLComment.class);
            tokens.add(MLComment.class);
            tokens.add(Whitespace.class);
            tokens.add(OpenSquare.class);
            tokens.add(OpenParentheses.class);
            return tokens;
        }

        @SuppressWarnings("unchecked")
        private Parsed<S> unexpectedValueToken(Token<S> token) {
            return unexpectedToken(token, valueTokens().toArray(new Class[0]));
        }

        @Override
        public Parsed<S> input(Token<S> token) {
            if (token == null) throw new IllegalArgumentException("token==null");

            switch (state) {
                case ExpectValue -> {
                    if (token instanceof MLComment<S> t) {
                        if (!options.multiLineComment) return unexpectedValueToken(t);
                        return ok(this);
                    } else if (token instanceof SLComment<S> t) {
                        if (!options.singleLineComment) return unexpectedValueToken(t);
                        return ok(this);
                    } else if (token instanceof Whitespace<S> t) {
                        return ok(this);
                    } else if (token instanceof BigIntToken<S> t) {
                        state = State.AfterValue;
                        values.add(new Ast.NumberAst.BigIntAst<>(t));
                        return ok(this);
                    } else if (token instanceof LongToken<S> t) {
                        state = State.AfterValue;
                        values.add(new Ast.NumberAst.LongAst<>(t));
                        return ok(this);
                    } else if (token instanceof IntToken<S> t) {
                        state = State.AfterValue;
                        values.add(new Ast.NumberAst.IntAst<>(t));
                        return ok(this);
                    } else if (token instanceof DoubleToken<S> t) {
                        state = State.AfterValue;
                        values.add(new Ast.NumberAst.DoubleAst<>(t));
                        return ok(this);
                    } else if (token instanceof NullToken<S> t) {
                        state = State.AfterValue;
                        values.add(new Ast.NullAst<>(t));
                        return ok(this);
                    } else if (token instanceof FalseToken<S> t) {
                        state = State.AfterValue;
                        values.add(new Ast.BooleanAst.FalseAst<>(t));
                        return ok(this);
                    } else if (token instanceof TrueToken<S> t) {
                        state = State.AfterValue;
                        values.add(new Ast.BooleanAst.TrueAst<>(t));
                        return ok(this);
                    } else if (token instanceof StringToken<S> t) {
                        state = State.AfterValue;
                        values.add(new Ast.StringAst<>(t));
                        return ok(this);
                    } else if (token instanceof IdentifierToken<S> t) {
                        if (!options.identInArrayValue)
                            return unexpectedValueToken(t);

                        state = State.AfterValue;
                        values.add(new Ast.IdentAst<>(t));
                        return ok(this);
                    } else if (token instanceof OpenSquare<S> t) {
                        state = State.AfterValue;
                        return ok(new ArrayParser<S>(options, this, t, values::add));
                    } else if (token instanceof OpenParentheses<S> t) {
                        state = State.AfterValue;
                        return ok(new ObjectParser<S>(options, this, t, values::add));
                    } else if (token instanceof CloseSquare<S> t) {
                        var res = new Ast.ArrayAst<>(
                            ImList.from(values),
                            begin,
                            t
                        );

                        resultConsumer.accept(res);
                        state = State.AfterValue;

                        boolean nested = !(parent instanceof AstParser.Init<S>);
                        if (nested) {
                            if (options.returnNestedValue) {
                                return ok(parent, res);
                            } else {
                                return ok(parent);
                            }
                        } else {
                            return ok(parent, res);
                        }
                    } else if (token instanceof CloseParentheses<S> t) {
                        return err("!!");
                    } else if (token instanceof Colon<S> t) {
                        return err("!!");
                    } else if (token instanceof Comma<S> t) {
                        return err("!!");
                    }
                }
                case AfterValue -> {
                    if (token instanceof CloseSquare<S> t) {
                        var res = new Ast.ArrayAst<>(
                            ImList.from(values),
                            begin,
                            t
                        );

                        resultConsumer.accept(res);
                        state = State.AfterValue;

                        boolean nested = !(parent instanceof AstParser.Init<S>);
                        if (nested) {
                            if (options.returnNestedValue) {
                                return ok(parent, res);
                            } else {
                                return ok(parent);
                            }
                        } else {
                            return ok(parent, res);
                        }
                    } else if (token instanceof Comma<S> t) {
                        state = State.ExpectValue;
                        return ok(this);
                    } else if (token instanceof MLComment<S> t) {
                        return ok(this);
                    } else if (token instanceof SLComment<S> t) {
                        return ok(this);
                    } else if (token instanceof Whitespace<S> t) {
                        return ok(this);
                    } else {
                        return err("!!");
                    }
                }
            }

            return err("!!");
        }
    }

    /**
     * Ошибка парсинга
     */
    public static class JsonParseError extends java.lang.Error {
        private final AstParser.Error<?> source;

        public AstParser.Error<?> getSource() {return source;}

        public JsonParseError(AstParser.Error<?> source) {
            super(source.message());
            this.source = source;
        }
    }

    /**
     * Ошибка парсинга - нет результата, наверно не все данные были переданы
     */
    public static class NoResult extends java.lang.Error {
        private final AstParser<?> parser;

        public AstParser<?> getParser() {
            return parser;
        }

        public NoResult(AstParser<?> parser) {
            super("no");
            this.parser = parser;
        }
    }

    public static Ast<StringPointer> parse(String source) {
        if (source == null) throw new IllegalArgumentException("source==null");
        var tokens = Tokenizer.parse(source);

        AstParser<StringPointer> parser = new AstParser.Init<>();
        for (var token : tokens.tokens()) {
            var res = parser.input(token);
            if (res.parser() instanceof AstParser.Error<StringPointer> err) {
                throw new JsonParseError(err);
            }
            if (res.result().isPresent()) {
                return res.result().get();
            }
            parser = res.parser();
        }

        throw new NoResult(parser);
    }
}
