package xyz.cofe.json.stream.parser.grammar;

import xyz.cofe.coll.im.ImList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Рекурсивная ссылка
 *
 * @param revPath реверсивный путь (от конца к началу)
 */
public record RecursiveRef(ImList<PathNode> revPath) {
    /**
     * Начальное правило с которого начинается путь
     *
     * @return правило
     */
    public Optional<Grammar.Rule> startRule() {
        return revPath.last().map(n -> n.rule);
    }

    /**
     * Последняя ссылка в пути, которая формирует цикл
     *
     * @return ссылка
     */
    public Optional<Grammar.Ref> lastRef() {
        return revPath.head().map(n -> n.ref);
    }

    /**
     * Узел пути
     *
     * @param rule Правило в котором есть ссылка
     * @param ref  ссылка на другое правило
     */
    public record PathNode(Grammar.Rule rule, Grammar.Ref ref) {}

    private static final Map<Grammar,ImList<RecursiveRef>> cache = new WeakHashMap<>();

    /**
     * Поиск рекурсивных ссылок в грамматике
     *
     * @param grammar грамматика
     * @return Рекурсивные ссылки
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static ImList<RecursiveRef> find(Grammar grammar) {
        if (grammar == null) throw new IllegalArgumentException("grammar==null");

        var cached = cache.get(grammar);
        if( cached!=null )return cached;

        var cycles = new ArrayList<RecursiveRef>();

        grammar.rules().each(start -> {
            var visitedRuleName = new HashSet<String>();
            visitedRuleName.add(start.name());

            var workSet = start.definition().walk().go()
                .fmap(Grammar.Ref.class)
                .map(d -> ImList.of(new PathNode(start, d)));

            while (workSet.size() > 0) {
                var headPath = workSet.head().get();

                workSet = workSet.tail();

                var headRef = headPath.head().get();
                if (visitedRuleName.contains(headRef.ref.name())) {
                    // cycle detect
                    cycles.add(new RecursiveRef(headPath));
                } else {
                    var follow = grammar.rule(headRef.ref.name()).fmap(rule -> {
                        return rule.definition().walk().go()
                            .fmap(Grammar.Ref.class)
                            .map(d -> ImList.of(headPath.prepend(new PathNode(rule, d))));
                    });

                    workSet = workSet.prepend(follow);

                    visitedRuleName.add(headRef.ref.name());
                }
            }
        });

        var removeSet = getInvalidPaths(cycles);
        cycles.removeAll(removeSet);

        var result = ImList.of(cycles);
        cache.put(grammar, result);

        return result;
    }

    private static HashSet<RecursiveRef> getInvalidPaths(ArrayList<RecursiveRef> cycles) {
        var removeSet = new HashSet<RecursiveRef>();
        for (var oneCycle : cycles) {
            if (oneCycle.startRule().isEmpty()) {
                removeSet.add(oneCycle);
                continue;
            }
            if(oneCycle.lastRef().isEmpty()){
                removeSet.add(oneCycle);
                continue;
            }

            var start = oneCycle.startRule().get();
            var lastRef = oneCycle.lastRef().get();
            if( !start.name().equals(lastRef.name()) ){
                removeSet.add(oneCycle);
            }
        }
        return removeSet;
    }
}
