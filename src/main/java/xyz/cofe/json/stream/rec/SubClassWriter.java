package xyz.cofe.json.stream.rec;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.token.DummyCharPointer;

import java.util.Arrays;

public interface SubClassWriter {
    Ast<DummyCharPointer> write(Ast<DummyCharPointer> ast, Object value);

    public static SubClassWriter defaultWriter(RecMapper mapper) {
        return ((ast, value) -> {
            Class<?> cls = value.getClass();
            var itfs = Arrays.stream(cls.getInterfaces()).filter(Class::isSealed).toList();

            if (itfs.size() == 1) {
                var name = cls.getSimpleName();
                return Ast.ObjectAst.create(
                    ImList.of(
                        Ast.KeyValue.create(mapper.toAst(name), ast
                        )));
            }
            
            return ast;
        });
    }


}
