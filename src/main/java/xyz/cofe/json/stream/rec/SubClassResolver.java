package xyz.cofe.json.stream.rec;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Result;
import xyz.cofe.json.stream.ast.Ast;

import java.lang.reflect.Type;
import java.util.Optional;

public interface SubClassResolver {
    record Resolved(Ast<?> body, Class<?> klass) {}

    Result<Resolved,String> resolve(Ast<?> ast, Class<?>[] subclasses);

    static SubClassResolver defaultResolver(){
        return (ast, subclasses) -> {
            if( ast instanceof Ast.ObjectAst<?> objAst ) {
                for (var subCls : subclasses) {
                    var bodyOpt = objAst.get(subCls.getSimpleName());
                    if (bodyOpt.isPresent()) {
                        return Result.ok(new Resolved(bodyOpt.get(), subCls));
                    }
                }
                return Result.error("expect " +
                    "key/property:" + ImList.of(subclasses).map(Class::getSimpleName).foldLeft("", (sum, it)->sum.isBlank() ? it : sum+", "+it)+
                    ", actual:");
            } else {
                return Result.error("expect AstBody, actual: "+ast.getClass());
            }
        };
    }
}
