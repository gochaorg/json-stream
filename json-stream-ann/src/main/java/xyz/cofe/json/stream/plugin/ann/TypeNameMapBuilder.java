package xyz.cofe.json.stream.plugin.ann;

import java.util.Map;

public interface TypeNameMapBuilder {
    TypeNameMapper build(Map<Class<?>,String> writeMap, Map<String,Class<?>> readMap);
}
