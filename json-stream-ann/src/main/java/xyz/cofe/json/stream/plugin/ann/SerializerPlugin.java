package xyz.cofe.json.stream.plugin.ann;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.cofe.json.stream.rec.StdMapper;
import xyz.cofe.json.stream.rec.spi.StdMapperAdHocConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SerializerPlugin implements StdMapperAdHocConfig {
    private static final Logger log = LoggerFactory.getLogger(SerializerPlugin.class);

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
            var serAnn = recCmpt.getAnnotation(Serializer.class);
            var deSerAnn = recCmpt.getAnnotation(Deserializer.class);

            if (serAnn != null) {
                var instOpt = instanceOf(serAnn.value());
                if (instOpt.isPresent()) {
                    var inst = instOpt.get();

                    if (inst instanceof ISerializer<?> ser) {
                        @SuppressWarnings("rawtypes") ISerializer iser = ser;

                        mapper.fieldSerialize(recordClass, recCmpt.getName())
                            .valueMapper((obj,stack) -> {
                                //noinspection unchecked
                                return iser.serialize(obj, stack);
                            })
                            .append();
                    }
                }
            }

            if (deSerAnn != null) {
                var instOpt = instanceOf(deSerAnn.value());
                if (instOpt.isPresent()) {
                    var inst = instOpt.get();

                    if (inst instanceof IDeserializer<?> dser) {
                        @SuppressWarnings("rawtypes") IDeserializer idser = dser;

                        //noinspection unchecked
                        mapper.fieldDeserialize(recordClass, recCmpt.getName())
                            .deserialize((ast, stacks) -> idser.deserialize(ast, stacks))
                            .append();
                    }
                }
            }
        }
    }

    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Optional<Object> instanceOf(Class<?> cls) {
        var inst0 = instances.get(cls);
        if (inst0 != null) return Optional.of(inst0);

        synchronized (this) {
            var inst1 = instances.get(cls);
            if (inst1 != null) return Optional.of(inst1);

            try {
                var inst = cls.getDeclaredConstructor().newInstance();
                instances.put(cls, inst);
                return Optional.of(inst);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                log.error("can't create instance " + cls, e);
                return Optional.empty();
            }
        }
    }
}
