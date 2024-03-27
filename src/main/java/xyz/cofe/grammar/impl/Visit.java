package xyz.cofe.grammar.impl;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.grammar.Grammar;

import java.util.WeakHashMap;

public class Visit {
    public static final WeakHashMap<Grammar.Definition, ImList<Grammar.Definition>> nestedCache = new WeakHashMap<>();
    public static final WeakHashMap<Grammar.Definition, ImList<Grammar.Definition.DefPath>> nestedPathCache = new WeakHashMap<>();
}
