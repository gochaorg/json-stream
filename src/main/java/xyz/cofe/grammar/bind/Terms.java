package xyz.cofe.grammar.bind;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Terms {
    Class<?>[] value();
}
