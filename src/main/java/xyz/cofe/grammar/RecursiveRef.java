package xyz.cofe.grammar;

import xyz.cofe.coll.im.ImList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Рекурсивная ссылка
 * <p>
 * Может быть обычной рекурсивной ссылкой или "левой" рекурсией.
 * <p>
 * Левая рекурсия когда правило имеет такой вывод: A → At
 *
 * @param revPath реверсивный путь (от конца к началу)
 */
public record RecursiveRef(ImList<RecursiveNode> revPath) {
    /**
     * Начальное правило с которого начинается путь
     *
     * @return правило
     */
    public Optional<Grammar.Rule> startRule() {
        return revPath.last().map(n -> n.rule());
    }

    /**
     * Начальная ссылка в пути
     *
     * @return ссылка
     */
    public Optional<Grammar.Ref> startRef() {
        return revPath.foldRight(Optional.empty(), (acc, it) ->
            acc.isPresent() ? acc :
                it.defPath().definition() instanceof Grammar.Ref ref ?
                    Optional.of(ref) : Optional.empty());
    }

    /**
     * Последняя ссылка в пути, которая формирует цикл
     *
     * @return ссылка
     */
    public Optional<Grammar.Ref> lastRef() {
        return revPath.fmap(n -> n.defPath().definition() instanceof Grammar.Ref r ? ImList.of(r) : ImList.of()).head();
    }

    /**
     * Узел содержащий последнюю ссылку
     *
     * @return узел пути
     */
    @SuppressWarnings("unused")
    public Optional<RecursiveNode> lastRefNode() {
        return revPath().head();
    }

    /**
     * Рекурсия является "левой"
     *
     * @return true - левая рекурсия
     */
    public boolean isLeftRecursion() {
        return lastRefNode().map(n -> n.offset() == 0).orElse(false);
    }

    @Override
    public String toString() {
        return "recursive ref" + (isLeftRecursion() ? "(left recursion)" : "") + ": rule="
            + startRule().map(Grammar.Rule::name).orElse("?")
            + " path=" + revPath().map(
                RecursiveNode::toString
            ).reverse()
            .foldLeft("", (acc, it) -> acc.isBlank() ? it : acc + " > " + it)
            ;
    }

    private static final Map<Grammar, ImList<RecursiveRef>> cache = new WeakHashMap<>();

    /**
     * Поиск рекурсивных ссылок в грамматике
     *
     * @param grammar грамматика
     * @return Рекурсивные ссылки
     */
    public static ImList<RecursiveRef> find(Grammar grammar) {
        if (grammar == null) throw new IllegalArgumentException("grammar==null");

        var cached = cache.get(grammar);
        if (cached != null) return cached;

        var cycles = new ArrayList<RecursiveRef>();

        grammar.rules().each(start -> {
            cycles.addAll(RecursiveVisitor.visit(grammar, start).cycles().toList());
        });

        var removeSet = getInvalidPaths(cycles);
        cycles.removeAll(removeSet);

        var result = ImList.from(cycles);
        cache.put(grammar, result);

        return result;
    }

    private static HashSet<RecursiveRef> getInvalidPaths(ArrayList<RecursiveRef> cycles) {
        var removeSet = new HashSet<RecursiveRef>();
        for (var oneCycle : cycles) {
            if (oneCycle.startRef().isEmpty()) {
                removeSet.add(oneCycle);
                continue;
            }
            if (oneCycle.lastRef().isEmpty()) {
                removeSet.add(oneCycle);
                continue;
            }

            var startRef = oneCycle.startRef().get();
            var lastRef = oneCycle.lastRef().get();

            if (!startRef.name().equals(lastRef.name())) {
                removeSet.add(oneCycle);
            }
        }
        return removeSet;
    }
}
