package xyz.cofe.grammar;

import xyz.cofe.coll.im.ImList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Дублирование имени правила
 * @param rule правило
 */
public record DuplicateRuleName(Grammar.Rule rule) {
    private static final WeakHashMap<Grammar, ImList<DuplicateRuleName>> duplicates = new WeakHashMap<>();

    private static final WeakHashMap<Grammar, Map<String,ImList<Grammar.Rule>>> ruleMap = new WeakHashMap<>();

    /**
     * Возвращает карту правил
     * @param grammar грамматика
     * @return правила
     */
    public static Map<String,ImList<Grammar.Rule>> ruleMapOf(Grammar grammar){
        if( grammar==null ) throw new IllegalArgumentException("grammar==null");

        var cache = ruleMap.get(grammar);
        if( cache!=null ){
            return cache;
        }

        var m = new HashMap<String, List<Grammar.Rule>>();
        grammar.rules().each(rule1 -> {
            m.computeIfAbsent(rule1.name(), x -> new ArrayList<>()).add(rule1);
        });

        var m2 = new HashMap<String,ImList<Grammar.Rule>>();
        m.forEach( (n,l) -> m2.put(n, ImList.from(l)) );

        ruleMap.put(grammar, m2);
        return m2;
    }

    /**
     * Ищет дубликаты (по имени) правил
     * @param grammar грамматика
     * @return дубликаты
     */
    public static ImList<DuplicateRuleName> find(Grammar grammar){
        if( grammar==null ) throw new IllegalArgumentException("grammar==null");

        var cached = duplicates.get(grammar);
        if( cached!=null )return cached;

        var m = new HashMap<String, List<Grammar.Rule>>();
        grammar.rules().each(rule1 -> {
            m.computeIfAbsent(rule1.name(), x -> new ArrayList<>()).add(rule1);
        });

        var dup = ImList.<DuplicateRuleName>of();
        for( var e: m.entrySet() ){
            if( e.getValue().size()>1 ){
                for( Grammar.Rule r : e.getValue() ){
                    dup = dup.prepend(new DuplicateRuleName(r));
                }
            }
        }

        duplicates.put(grammar, dup);
        return dup;
    }
}
