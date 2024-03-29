package xyz.cofe.grammar;

import xyz.cofe.coll.im.ImList;

import java.util.List;

import static xyz.cofe.grammar.Grammar.*;

public record First(Term term, Rule rule) {
    public record Path(Rule rule, Definition def) {}

    public static ImList<First> find(Rule rule, Grammar grammar){
//        ImList<Path> workList = rule.definition().walk().go().map( d -> new Path(rule,d) );
//        while (workList.size()>0){
//            var h =
//        }
        return null;
    }
}
