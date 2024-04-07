package xyz.cofe.grammar.lr;

import java.util.Optional;

public interface TokenParser<T, S extends SourcePointer<Character, S>> {
    public Optional<ParsedToken<T, S>> input(S src);
    public void reset();
    public boolean hasState();
}
