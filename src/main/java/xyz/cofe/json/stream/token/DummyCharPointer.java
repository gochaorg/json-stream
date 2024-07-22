package xyz.cofe.json.stream.token;

import java.util.Optional;

public final class DummyCharPointer implements CharPointer<DummyCharPointer> {
    public final static DummyCharPointer instance = new DummyCharPointer();

    private DummyCharPointer(){
    }

    @Override
    public Optional<Character> get(int offset) {
        return Optional.empty();
    }

    @Override
    public DummyCharPointer move(int offset) {
        return this;
    }

    @Override
    public int subtract(DummyCharPointer other) {
        return 0;
    }
}
