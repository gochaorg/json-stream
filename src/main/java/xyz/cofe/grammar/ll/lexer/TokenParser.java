package xyz.cofe.grammar.ll.lexer;

import xyz.cofe.grammar.ll.bind.TermBind;

import java.lang.reflect.Type;
import java.util.Optional;

public sealed interface TokenParser permits TokenParserEnumValue,
                                            TokenParserStaticMethod {
    TermBind[] binds();

    Type tokenType();

    Optional<Matched<?>> parse(String source, int offset);

    boolean hasSkip();
}
