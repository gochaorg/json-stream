package xyz.cofe.json.stream.rec;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Result;
import xyz.cofe.json.stream.ast.Ast;

public interface SubClassResolver {
    record Resolved(Ast<?> body, Class<?> klass) {}

    Result<Resolved,String> resolve(Ast<?> ast, Class<?> parentClass, Class<?>[] subClasses, ImList<RecMapper.ParseStack> stack);

    static SubClassResolver defaultResolver(){
        return (ast, parentClass, subclasses, stack) -> {
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

    static SubClassResolver typeProperty(String propertyName){
        if( propertyName==null ) throw new IllegalArgumentException("propertyName==null");
        return (ast, parentClass, subClasses, stack) -> {
            if( ast instanceof Ast.ObjectAst<?> objAst ) {
                return Result.from(
                    objAst.get(propertyName).flatMap(Ast::asString),
                    ()->"@type not found"
                ).fmap( typeName -> {
                    for (var subCls : subClasses) {
                        if(subCls.getSimpleName().equals(typeName)){
                            return Result.ok(subCls);
                        }
                    }
                    return Result.error("type not found "+typeName);
                }).map( cls -> new Resolved(ast, cls) );
            }else {
                return Result.error("expect AstBody, actual: "+ast.getClass());
            }
        };
    }
}
