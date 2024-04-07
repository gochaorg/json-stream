package xyz.cofe.grammar.lr;

import java.util.Optional;

public interface SourcePointer<C, SELF extends SourcePointer<C, SELF>> {
    SELF move(int offset);

    Optional<C> get();

    int substract(SELF other);
}
