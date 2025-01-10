package xyz.cofe.json.stream.rec;

import xyz.cofe.coll.im.Fn1;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.token.DummyCharPointer;
import xyz.cofe.json.stream.token.StringToken;

import java.util.function.Function;

public interface SubClassWriter {
    Ast<DummyCharPointer> write(Ast<DummyCharPointer> ast, Object value, RecMapper mapper, ImList<RecMapper.ToAstStack> stack);

    static final SubClassWriter defaultWriter = conditional(
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

    static ImList<Class<?>> getSealedInterface(Class<?> cls){
        if( cls==null ) throw new IllegalArgumentException("cls==null");
        return ImList.of(cls.getInterfaces())
            .filter(Class::isSealed);
    }

    static SubClassWriter conditional(Fn1<Object, Boolean> condition, SubClassWriter trueWriter, SubClassWriter falseWriter){
        if( condition==null ) throw new IllegalArgumentException("condition==null");
        if( trueWriter==null ) throw new IllegalArgumentException("trueWriter==null");
        if( falseWriter==null ) throw new IllegalArgumentException("falseWriter==null");

        return (ast, value, mapper, stack) ->
            condition.apply(value)
                ? trueWriter.write(ast,value,mapper,stack)
                : falseWriter.write(ast,value,mapper,stack);
    }

    static SubClassWriter asIs(){
        return (ast, value, mapper, stack) -> ast;
    }

    static SubClassWriter simpleClassName() {
        return simpleClassName(Class::getSimpleName);
    }

    static SubClassWriter simpleClassName(Function<Class<?>, String> resolveTypeName) {
        if( resolveTypeName==null ) throw new IllegalArgumentException("resolveTypeName==null");
        return (ast, value, mapper, stack) -> {
            Class<?> cls = value.getClass();
            var name = resolveTypeName.apply(cls);
            return Ast.ObjectAst.create(
                ImList.of(
                    Ast.KeyValue.create(mapper.toAst(name), ast
                    )));
        };
    }

    static SubClassWriter typeProperty(String propertyName){
        if( propertyName==null ) throw new IllegalArgumentException("propertyName==null");
        return typeProperty(propertyName, Class::getSimpleName);
    }

    static SubClassWriter typeProperty(String propertyName, Function<Class<?>,String> resolveTypeName){
        if( propertyName==null ) throw new IllegalArgumentException("propertyName==null");
        if( resolveTypeName==null ) throw new IllegalArgumentException("resolveTypeName==null");
        return (ast, value, mapper, stack) -> {
            if( ast instanceof Ast.ObjectAst<DummyCharPointer> aObj ){
                Class<?> cls = value.getClass();
                var name = resolveTypeName.apply(cls);
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
