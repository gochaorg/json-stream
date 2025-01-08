package xyz.cofe.json.stream.plugin.ann;

import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.token.DummyCharPointer;

public interface ISerializer<T> {
    Ast<DummyCharPointer> serialize(T value);
}
