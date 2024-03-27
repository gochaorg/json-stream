package xyz.cofe.grammar.lexer;

import xyz.cofe.grammar.bind.TermBind;

import java.lang.reflect.Type;
import java.util.Optional;

public sealed interface TokenParser permits TokenParserEnumValue,
                                            TokenParserStaticMethod {
    TermBind[] binds();

    Type tokenType();

    Optional<Matched<?>> parse(String source, int offset);

    boolean hasSkip();
}
