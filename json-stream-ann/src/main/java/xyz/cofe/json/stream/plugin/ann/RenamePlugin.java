package xyz.cofe.json.stream.plugin.ann;

import xyz.cofe.json.stream.rec.StdMapper;
import xyz.cofe.json.stream.rec.spi.StdMapperAdHocConfig;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class RenamePlugin implements StdMapperAdHocConfig {
    @Override
    public void configure(StdMapper mapper, Type type) {
        if (mapper != null && type instanceof Class<?> cls && cls.isRecord()) {
            configureRecord(mapper, cls);
        }
    }

    private final Set<Class<?>> configuredRecords = new HashSet<>();

    private void configureRecord(StdMapper mapper, Class<?> recordClass) {
        if (configuredRecords.contains(recordClass)) return;
        configuredRecords.add(recordClass);

        var recComponents = recordClass.getRecordComponents();
        for (var recCmpt : recComponents) {
            var ann = recCmpt.getAnnotation(Rename.class);
            if( ann==null )continue;

            if( !ann.serializeName().isBlank() ){
                mapper.fieldSerialize(recordClass, recCmpt.getName())
                    .rename(ann.serializeName())
                    .append();

                mapper.fieldDeserialize(recordClass, recCmpt.getName())
                    .name(ann.value(), ann.otherNames())
                    .append();
            }else{
                if( !ann.value().isBlank() ){
                    mapper.fieldSerialize(recordClass, recCmpt.getName())
                        .rename(ann.value())
                        .append();

                    mapper.fieldDeserialize(recordClass, recCmpt.getName())
                        .name(ann.value(), ann.otherNames())
                        .append();
                }
            }
        }
    }
}
