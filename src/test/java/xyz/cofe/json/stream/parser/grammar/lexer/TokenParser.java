package xyz.cofe.json.stream.parser.grammar.lexer;

import xyz.cofe.json.stream.parser.grammar.bind.TermBind;

import java.lang.reflect.Type;
import java.util.Optional;

public sealed interface TokenParser permits TokenParserEnumValue,
                                            TokenParserStaticMethod {
    TermBind[] binds();

    Type tokenType();

    Optional<Matched<?>> parse(String source, int offset);

    boolean hasSkip();
}
