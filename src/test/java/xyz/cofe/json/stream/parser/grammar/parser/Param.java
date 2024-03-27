package xyz.cofe.json.stream.parser.grammar.parser;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.parser.grammar.lexer.TokenParser;

/**
 * Принимаемый параметр
 */
sealed public interface Param {
    Class<?> node();

    /**
     * Ссылка на терминал
     *
     * @param node    тип
     * @param parsers парсеры
     */
    record TermRef(Class<?> node, ImList<TokenParser> parsers) implements Param {}

    /**
     * Ссылка на не-терминал
     *
     * @param node    тип
     * @param parsers парсеры
     */
    record RuleRef(Class<?> node, ImList<StaticMethodParser> parsers) implements Param {}
}
