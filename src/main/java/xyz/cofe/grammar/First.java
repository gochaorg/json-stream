package xyz.cofe.grammar;

import xyz.cofe.coll.im.ImList;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import static xyz.cofe.grammar.Grammar.*;

public record First(Rule rule, Term term) {
    private static final WeakHashMap<Grammar, WeakHashMap<Rule, ImList<First>>> cache = new WeakHashMap<>();

    public static ImList<First> find(Grammar grammar, Rule rule){
        if( grammar==null ) throw new IllegalArgumentException("grammar==null");
        if( rule==null ) throw new IllegalArgumentException("rule==null");

        var c1 = cache.get(grammar);
        if( c1!=null ){
            var c2 = c1.get(rule);
            if( c2!=null )return c2;
        }

        List<First> first = new ArrayList<>();

        RecursiveVisitor.visit(grammar, rule, recursivePath -> {
            recursivePath.revPath().head().ifPresent( recursiveNode -> {
                System.out.println(
                    "["+recursiveNode.offset()+"] "+
                        recursiveNode.defPath().definition()+
                        " "+recursivePath
                );
                var def = recursiveNode.defPath().definition();
                if( def instanceof Term term && recursiveNode.offset()==0 ){
                    first.add(new First(recursiveNode.rule(), term));
                }
            });
        });

        ImList<First> result = ImList.of(first);
        cache.computeIfAbsent(grammar, ignore -> new WeakHashMap<>()).put(rule, result);

        return result;
    }
}
