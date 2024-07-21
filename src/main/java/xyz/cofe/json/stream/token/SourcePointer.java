package xyz.cofe.json.stream.token;

import java.util.Optional;

public sealed interface SourcePointer<C, SELF extends SourcePointer<C,SELF>> permits CharPointer {
    Optional<C> get(int offset);
    SELF move(int offset);
    int subtract(SELF other);
}
