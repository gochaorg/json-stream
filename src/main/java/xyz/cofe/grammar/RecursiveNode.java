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
        var defTxt = switch (defPath.definition()) {
            case Grammar.Ref(var r) -> "ref(" + r + " ";
            case Grammar.Term(var t) -> "term(" + t + " ";
            case Grammar.Alternative ignored -> "alt(";
            case Grammar.Repeat ignored -> "repeat(";
            case Grammar.Sequence ignored -> "sequence(";
        };
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
