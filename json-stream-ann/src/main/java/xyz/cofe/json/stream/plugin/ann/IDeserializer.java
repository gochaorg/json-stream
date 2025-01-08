package xyz.cofe.json.stream.plugin.ann;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Result;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.rec.RecMapParseError;
import xyz.cofe.json.stream.rec.RecMapper;

public interface IDeserializer<T> {
    Result<T, RecMapParseError> deserialize(Ast<?> ast, ImList<RecMapper.ParseStack> stack);
}
