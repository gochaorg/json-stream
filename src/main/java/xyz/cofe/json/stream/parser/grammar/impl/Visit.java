package xyz.cofe.json.stream.parser.grammar.impl;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.parser.grammar.Grammar;

import java.util.WeakHashMap;

public class Visit {
    public static final WeakHashMap<Grammar.Definition, ImList<Grammar.Definition>> nestedCache = new WeakHashMap<>();
}
