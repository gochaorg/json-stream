package xyz.cofe.grammar.bind;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(TermBinds.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface TermBind {
    String value();
    boolean skip() default false;
}
