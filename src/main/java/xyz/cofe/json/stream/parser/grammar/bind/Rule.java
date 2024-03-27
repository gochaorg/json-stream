package xyz.cofe.json.stream.parser.grammar.bind;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Rule {
    int order() default 0;
}
