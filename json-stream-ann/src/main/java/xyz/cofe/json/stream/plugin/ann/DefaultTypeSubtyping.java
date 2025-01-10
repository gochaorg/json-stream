package xyz.cofe.json.stream.plugin.ann;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Result;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.rec.RecMapper;
import xyz.cofe.json.stream.rec.SubClassResolver;
import xyz.cofe.json.stream.rec.SubClassWriter;
import xyz.cofe.json.stream.token.DummyCharPointer;

import java.util.Map;

public class DefaultTypeSubtyping implements TypeNameMapper {
    private final Map<Class<?>,String> writeMap;
    private final Map<String,Class<?>> readMap;
    private final SubClassWriter subClassWriter;
    private final SubClassResolver subClassResolver;

    public DefaultTypeSubtyping(Map<Class<?>,String> writeMap, Map<String,Class<?>> readMap){
        if( writeMap==null ) throw new IllegalArgumentException("writeMap==null");
        if( readMap==null ) throw new IllegalArgumentException("readMap==null");

        this.readMap = readMap;
        this.writeMap = writeMap;

        subClassResolver = SubClassResolver.defaultResolver(readMap);
        subClassWriter = SubClassWriter.simpleClassName( cls -> writeMap.getOrDefault(cls, cls.getSimpleName()));
    }

    @Override
    public Result<Resolved, String> resolve(Ast<?> ast, Class<?> parentClass, Class<?>[] subClasses, ImList<RecMapper.ParseStack> stack) {
        return subClassResolver.resolve(ast, parentClass, subClasses, stack);
    }

    @Override
    public Ast<DummyCharPointer> write(Ast<DummyCharPointer> ast, Object value, RecMapper mapper, ImList<RecMapper.ToAstStack> stack) {
        return subClassWriter.write(ast, value, mapper, stack);
    }
}
