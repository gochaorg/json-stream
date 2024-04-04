package xyz.cofe.grammar.ll.parser;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.grammar.ll.lexer.TokenParser;

import java.util.Objects;

/**
 * Принимаемый параметр
 */
sealed public interface Param {
    sealed public interface RepeatableParam extends Param {}

    /**
     * Тип узла (терминала или не-терминала)
     * @return тип
     */
    Class<?> node();

    /**
     * Ссылка на терминал
     *
     * @param node    тип
     * @param parsers парсеры
     */
    record TermRef(Class<?> node, ImList<TokenParser> parsers) implements Param, RepeatableParam {}

    /**
     * Ссылка на не-терминал
     *
     * @param node    тип
     * @param parsers парсеры
     */
    record RuleRef(Class<?> node, ImList<StaticMethodParser> parsers) implements Param, RepeatableParam {}

    /**
     * Повтор 0 или более раз
     * @param param параметр
     */
    record Repeat(RepeatableParam param) implements Param {
        public Repeat {
            Objects.requireNonNull(param);
        }

        @Override
        public Class<?> node() {
            return param.node();
        }
    }
}
