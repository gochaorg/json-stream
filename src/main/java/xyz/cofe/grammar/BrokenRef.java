package xyz.cofe.grammar;

import xyz.cofe.coll.im.ImList;

import java.util.ArrayList;

/**
 * Поиск биты ссылок в грамматике
 * @param rule имя правила
 * @param ref битая ссылка
 */
public record BrokenRef(Grammar.Rule rule, Grammar.Ref ref) {

    /**
     * Поиск битых ссылок
     * @param grammar грамматика
     * @return битые ссылки
     */
    public static ImList<BrokenRef> find(Grammar grammar){
        if( grammar==null ) throw new IllegalArgumentException("grammar==null");
        var res = new ArrayList<BrokenRef>();

        grammar.rules().each( rule -> {
            for( var d : rule.definition().walk().go() ){
                if( d instanceof Grammar.Ref ref ){
                    var rules = grammar.rule(ref.name());
                    if( rules.size()<1 ){
                        res.add(new BrokenRef(rule, ref));
                    }
                }
            }
        });

        return ImList.from(res);
    }
}
