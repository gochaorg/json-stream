package xyz.cofe.json.stream.rec;

import xyz.cofe.coll.im.Fn1;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.token.DummyCharPointer;
import xyz.cofe.json.stream.token.StringToken;

public interface SubClassWriter {
    Ast<DummyCharPointer> write(Ast<DummyCharPointer> ast, Object value, RecMapper mapper);

    static SubClassWriter defaultWriter = conditional(
        hasAnySealedInterface(),
        simpleClassName(),
        asIs()
    );

    static Fn1<Object,Boolean> hasAnySealedInterface(){
        return value ->
            ImList.of(value.getClass().getInterfaces())
                .filter(Class::isSealed)
                .isNonEmpty();
    }

    static SubClassWriter conditional(Fn1<Object, Boolean> condition, SubClassWriter trueWriter, SubClassWriter falseWriter){
        if( condition==null ) throw new IllegalArgumentException("condition==null");
        if( trueWriter==null ) throw new IllegalArgumentException("trueWriter==null");
        if( falseWriter==null ) throw new IllegalArgumentException("falseWriter==null");

        return (ast, value, mapper) -> condition.apply(value) ? trueWriter.write(ast,value,mapper) : falseWriter.write(ast,value,mapper);
    }

    static SubClassWriter asIs(){
        return (ast, value, mapper) -> ast;
    }

    static SubClassWriter simpleClassName() {
        return (ast, value, mapper) -> {
            Class<?> cls = value.getClass();
            var name = cls.getSimpleName();
            return Ast.ObjectAst.create(
                ImList.of(
                    Ast.KeyValue.create(mapper.toAst(name), ast
                    )));
        };
    }

    static SubClassWriter typeProperty(String propertyName){
        if( propertyName==null ) throw new IllegalArgumentException("propertyName==null");
        return (ast, value, mapper) -> {
            if( ast instanceof Ast.ObjectAst<DummyCharPointer> aObj ){
                Class<?> cls = value.getClass();
                var name = cls.getSimpleName();
                return aObj.put(
                    Ast.StringAst.create(propertyName),
                    Ast.StringAst.create(name)
                );
            }else{
                throw new RecMapError("expect ast as object");
            }
        };
    }
}
