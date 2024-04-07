package xyz.cofe.json.stream;

import java.util.Optional;

public interface SourcePointer<C, SELF extends SourcePointer<C,SELF>> {
    Optional<C> get(int offset);
    SELF move(int offset);
    int subtract(SELF other);
}
