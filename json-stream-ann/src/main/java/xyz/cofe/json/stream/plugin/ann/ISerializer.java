package xyz.cofe.json.stream.plugin.ann;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.rec.RecMapper;
import xyz.cofe.json.stream.token.DummyCharPointer;

public interface ISerializer<T> {
    Ast<DummyCharPointer> serialize(T value, ImList<RecMapper.ToAstStack> stack);
}
