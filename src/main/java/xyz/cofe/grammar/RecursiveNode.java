package xyz.cofe.grammar;

/**
 * Узел пути
 *
 * @param rule    Правило в котором есть ссылка
 * @param defPath часть правила
 */
public record RecursiveNode(Grammar.Rule rule, Grammar.Definition.DefPath defPath, int offset) {
    @Override
    public String toString() {
        var de = defPath.definition();
        String defTxt = "";
        if( de instanceof Grammar.Ref a ) defTxt = "ref(" + a.name() + " ";
        else if( de instanceof Grammar.Term a ) defTxt = "term(" + a.text() + " ";
        else if( de instanceof Grammar.Alternative a ) defTxt = "alt(";
        else if( de instanceof Grammar.Repeat a ) defTxt = "repeat(";
        else if( de instanceof Grammar.Sequence a ) defTxt = "sequence(";

        return rule.name() + "/" + defTxt + "o=" + offset + ")" + "[" + rule.indexOf(defPath().definition()) + "]";
    }

    public static RecursiveNode of(Grammar.Rule rule) {
        if (rule == null) throw new IllegalArgumentException("rule==null");
        return new RecursiveNode(rule, Grammar.Definition.DefPath.of(rule), 0);
    }

    public static RecursiveNode of(Grammar.Rule rule, int offset) {
        if (rule == null) throw new IllegalArgumentException("rule==null");
        return new RecursiveNode(rule, Grammar.Definition.DefPath.of(rule), offset);
    }
}
