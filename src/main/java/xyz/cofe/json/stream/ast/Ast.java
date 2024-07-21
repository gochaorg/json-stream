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
import xyz.cofe.json.stream.token.MLComment;
import xyz.cofe.json.stream.token.NullToken;
import xyz.cofe.json.stream.token.OpenParentheses;
import xyz.cofe.json.stream.token.OpenSquare;
import xyz.cofe.json.stream.token.SLComment;
import xyz.cofe.json.stream.token.StringToken;
import xyz.cofe.json.stream.token.TrueToken;

/**
 * Все возможные узлы AST дерева Json
 * @param <S> Источник JSON
 */
public sealed interface Ast<S extends CharPointer<S>> {
    /**
     * Комментарий
     * @param <S>
     */
    sealed interface Comment<S extends CharPointer<S>> extends Ast<S> {
        /**
         * Многострочный комментарий
         * @param token
         * @param <S>
         */
        record MultiLine<S extends CharPointer<S>>(MLComment<S> token) implements Comment<S> {}

        /**
         * Однострочный комментарий
         * @param token
         * @param <S>
         */
        record SingleLine<S extends CharPointer<S>>(SLComment<S> token) implements Comment<S> {}
    }

    /**
     * Примитивное
     */
    sealed interface Primitive {}

    /**
     * Число
     * @param <S>
     */
    sealed interface NumberAst<S extends CharPointer<S>> extends Ast<S>,
                                                                 Primitive {
        /**
         * Число плавающее
         * @param token
         * @param <S>
         */
        record DoubleAst<S extends CharPointer<S>>(
            DoubleToken<S> token
        ) implements NumberAst<S> {}

        /**
         * Целое число (32 бит)
         * @param token
         * @param <S>
         */
        record IntAst<S extends CharPointer<S>>(IntToken<S> token) implements NumberAst<S> {}

        /**
         * Целое число (64 бит)
         * @param token
         * @param <S>
         */
        record LongAst<S extends CharPointer<S>>(LongToken<S> token) implements NumberAst<S> {}

        /**
         * Целое числов (дофига бит)
         * @param token
         * @param <S>
         */
        record BigIntAst<S extends CharPointer<S>>(BigIntToken<S> token) implements NumberAst<S> {}
    }

    /**
     * Булево значение
     * @param <S>
     */
    sealed interface BooleanAst<S extends CharPointer<S>> extends Ast<S>,
                                                                  Primitive {

        /**
         * True значение
         * @param token
         * @param <S>
         */
        record TrueAst<S extends CharPointer<S>>(TrueToken<S> token) implements BooleanAst<S> {}

        /**
         * False значение
         * @param token
         * @param <S>
         */
        record FalseAst<S extends CharPointer<S>>(FalseToken<S> token) implements BooleanAst<S> {}
    }

    /**
     * Null значение
     * @param token
     * @param <S>
     */
    record NullAst<S extends CharPointer<S>>(NullToken<S> token) implements Ast<S>,
                                                                            Primitive {}

    /**
     * Строка
     * @param token
     * @param <S>
     */
    record StringAst<S extends CharPointer<S>>(StringToken<S> token) implements Ast<S>,
                                                                                Primitive,
                                                                                Key<S> {}

    /**
     * Идентификатор
     * @param token
     * @param <S>
     */
    record IdentAst<S extends CharPointer<S>>(IdentifierToken<S> token) implements Ast<S>,
                                                                                   Primitive,
                                                                                   Key<S> {}

    sealed interface Key<S extends CharPointer<S>> extends Ast<S> {}

    /**
     * Пара ключ-значение
     * @param key ключ
     * @param value значение
     * @param <S>
     */
    record KeyValue<S extends CharPointer<S>>(Key<S> key, Ast<S> value) implements Ast<S> {}

    /**
     * Объект
     * @param values пары ключ-значение
     * @param begin начало объекта
     * @param end конец объекта
     * @param <S>
     */
    record ObjectAst<S extends CharPointer<S>>(
        ImList<KeyValue<S>> values,
        OpenParentheses<S> begin,
        CloseParentheses<S> end
    ) implements Ast<S> {}

    /**
     * Массив
     * @param values значения
     * @param begin начало массива
     * @param end конец массива
     * @param <S>
     */
    record ArrayAst<S extends CharPointer<S>>(
        ImList<Ast<S>> values, OpenSquare<S> begin, CloseSquare<S> end) implements Ast<S> {}
}
