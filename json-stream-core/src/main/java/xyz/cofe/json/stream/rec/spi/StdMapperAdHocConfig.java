package xyz.cofe.json.stream.rec.spi;

import xyz.cofe.json.stream.rec.StdMapper;

import java.lang.reflect.Type;

public interface StdMapperAdHocConfig {
    void configure(StdMapper mapper, Type type);
}
