package xyz.cofe.grammar.ll.bind;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TermBinds {
    TermBind[] value();
}
