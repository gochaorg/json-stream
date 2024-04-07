package xyz.cofe.json.stream.token;

import java.util.Optional;

public interface TokenParser<S extends CharPointer<S>> {
    public Optional<? extends Token<S>> parse(S ptr);
}
