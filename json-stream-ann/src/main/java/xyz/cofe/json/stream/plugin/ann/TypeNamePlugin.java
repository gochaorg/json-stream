package xyz.cofe.json.stream.plugin.ann;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.rec.StdMapper;
import xyz.cofe.json.stream.rec.SubClassWriter;
import xyz.cofe.json.stream.rec.spi.StdMapperAdHocConfig;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TypeNamePlugin implements StdMapperAdHocConfig {
    private final Set<Class<?>> configured = new HashSet<>();
    private final Set<String> configuredTypeNames = new HashSet<>();

    @Override
    public void configure(StdMapper mapper, Type type) {
        if( mapper!=null ) {
            if( configuredTypeNames.contains(type.toString()) )return;
            configuredTypeNames.add(type.toString());

            if( type instanceof ParameterizedType pt ){
                var paramTypes = pt.getActualTypeArguments();
                for( var pType : paramTypes ){
                    configure(mapper, pType);
                }
            }

            if (type instanceof Class<?> cls && !configured.contains(cls)) {
                if( cls.isSealed() ) {
                    configureSealedInterface(mapper, cls);
                }else{
                    configure(mapper, cls);
                }
                configured.add(cls);
            }
        }
    }

    private static ImList<Class<?>> getSealedInterface(Class<?> cls){
        if( cls==null ) throw new IllegalArgumentException("cls==null");
        return ImList.of(cls.getInterfaces()).filter(Class::isSealed);
    }

    private void configure(StdMapper mapper, Class<?> cls){
        var sealedItfs = getSealedInterface(cls);
        sealedItfs.each( sItf -> {
            configureSealedInterface(mapper, sItf);
        });
    }

    private void configureSealedInterface(StdMapper mapper, Class<?> sealedInterface) {
        Map<Class<?>,String> writeMap = new HashMap<>();
        Map<String,Class<?>> readMap = new HashMap<>();
        Set<Class<?>> nonNamed = new HashSet<>();

        for( var subType : sealedInterface.getPermittedSubclasses() ){
            configured.add(subType);

            var typeNameAnn = subType.getAnnotation(TypeName.class);
            if( typeNameAnn!=null ){
                if( typeNameAnn.value().length>0 ) {
                    var writeName = !typeNameAnn.writeName().isBlank()
                        ? typeNameAnn.writeName()
                        : !typeNameAnn.value()[0].isBlank()
                        ? typeNameAnn.value()[0]
                        : subType.getSimpleName();

                    writeMap.put(subType, writeName);

                    for (var readName0 : typeNameAnn.value()) {
                        var name = !readName0.isBlank()
                            ? readName0
                            : subType.getSimpleName();
                        readMap.put(name, subType);
                    }
                }else {
                    nonNamed.add(subType);
                }
            }else {
                writeMap.put(subType, subType.getSimpleName());
                readMap.put(subType.getSimpleName(), subType);
            }
        }

        DefaultTypeSubtyping typing = new DefaultTypeSubtyping(writeMap, readMap, nonNamed);
        mapper.subTypeResolve(sealedInterface).resolver(typing).append();

        for( var subType : sealedInterface.getPermittedSubclasses() ){
            if( !nonNamed.contains(subType) ) {
                mapper.subTypeWriter(subType).writer(typing).append();
            }else {
                mapper.subTypeWriter(subType).writer(typing).append();
            }
        }
    }
}
