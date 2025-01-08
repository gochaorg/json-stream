package xyz.cofe.json.stream.plugin.ann;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Deserializer {
    Class<?> value();
}
