package xyz.cofe.json.stream.plugin.ann;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.rec.StdMapper;
import xyz.cofe.json.stream.rec.spi.StdMapperAdHocConfig;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TypeNamePlugin implements StdMapperAdHocConfig {
    private final Set<Class<?>> configured = new HashSet<>();

    @Override
    public void configure(StdMapper mapper, Type type) {
        if( mapper!=null && type instanceof Class<?> cls && !configured.contains(cls) ){
            configure(mapper, cls);
            configured.add(cls);
        }
    }

    private static ImList<Class<?>> getSealedInterface(Class<?> cls){
        if( cls==null ) throw new IllegalArgumentException("cls==null");
        return ImList.of(cls.getInterfaces()).filter(Class::isSealed);
    }

    private void configure(StdMapper mapper, Class<?> cls){
        var sealedItfs = getSealedInterface(cls);
        //var subTypes  sealedItfs.fmap( itf -> ImList.of(itf.getPermittedSubclasses()) );
        sealedItfs.each( sItf -> {
            Map<Class<?>,String> writeMap = new HashMap<>();
            Map<String,Class<?>> readMap = new HashMap<>();

            for( var subType : sItf.getPermittedSubclasses() ){
                configured.add(subType);

                var typeNameAnn = subType.getAnnotation(TypeName.class);
                if( typeNameAnn!=null && typeNameAnn.value().length>0 ){
                    var writeName = !typeNameAnn.writeName().isBlank()
                        ? typeNameAnn.writeName()
                        : !typeNameAnn.value()[0].isBlank()
                        ? typeNameAnn.value()[0]
                        : subType.getSimpleName();

                    writeMap.put(subType, writeName);
                    for( var readName0 : typeNameAnn.value() ){
                        var name = !readName0.isBlank()
                            ? readName0
                            : subType.getSimpleName();
                        readMap.put(name, subType);
                    }
                }else {
                    writeMap.put(subType, subType.getSimpleName());
                    readMap.put(subType.getSimpleName(), subType);
                }
            }

            DefaultTypeSubtyping typing = new DefaultTypeSubtyping(writeMap, readMap);
            mapper.subTypeResolve(sItf).resolver(typing).append();

            //mapper.subTypeWriter(cls).writer(typing).append();
            for( var subType : sItf.getPermittedSubclasses() ){
                mapper.subTypeWriter(subType).writer(typing).append();
            }
        });
    }
}
