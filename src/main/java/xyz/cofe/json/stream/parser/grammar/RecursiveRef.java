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
        return revPath.fmap(n -> n.defPath().definition() instanceof Grammar.Ref r ? ImList.of(r) : ImList.of()).head();
    }

    /**
     * Узел содержащий последнюю ссылку
     *
     * @return узел пути
     */
    public Optional<PathNode> lastRefNode() {
        return revPath().head();
    }

    @Override
    public String toString() {
        return "recursive ref: rule="
            + startRule().map(Grammar.Rule::name).orElse("?")
            + " path=" + revPath().map(
                n -> n.toString() + "[" + n.rule().indexOf(n.defPath().definition()) + "]"
            ).reverse()
            .foldLeft("", (acc, it) -> acc.isBlank() ? it : acc + " > " + it)
            ;
    }

    /**
     * Узел пути
     *
     * @param rule Правило в котором есть ссылка
     * @param defPath часть правила
     */
    public record PathNode(Grammar.Rule rule, Grammar.Definition.DefPath defPath) {
        @Override
        public String toString() {
            var defTxt = switch (defPath.definition()){
                case Grammar.Ref(var r) -> "ref("+r+")";
                case Grammar.Term(var t) -> "term("+t+")";
                case Grammar.Alternative a -> "alt(...)";
                case Grammar.Repeat r -> "repeat()";
                case Grammar.Sequence s -> "sequence()";
            };
            return rule.name()+"/"+defTxt;
        }
    }

    private static final Map<Grammar, ImList<RecursiveRef>> cache = new WeakHashMap<>();

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
        if (cached != null) return cached;

        var cycles = new ArrayList<RecursiveRef>();

        grammar.rules().each(start -> {
            System.out.println("start "+start.name());

            var visitedRuleName = new HashSet<String>();
            visitedRuleName.add(start.name());

            var workSet = start.definition().walk().tree()
                .map(d -> ImList.of(new PathNode(start, d)));

            while (workSet.size() > 0) {
                var headPath = workSet.head().get();

                workSet = workSet.tail();

                var headNode = headPath.head().get();

                if(headNode.defPath.definition() instanceof Grammar.Ref ref) {
                    if (visitedRuleName.contains(ref.name())) {
                        // cycle detect
                        cycles.add(new RecursiveRef(headPath));
                    } else {
                        var follow = grammar.rule(ref.name()).fmap(rule -> {
                            return rule.definition().walk().tree()
                                .map(d -> ImList.of(headPath.prepend(new PathNode(rule, d))));
                        });

                        System.out.println("follow ("+follow.size()+") from "+headNode);
                        follow.each(path -> {
                            System.out.println(path.reverse()
                                .map(n -> n.toString())
                                .foldLeft("", (acc, it) -> !acc.isEmpty() ? acc + " > " + it : it)
                            );
                        });

                        workSet = workSet.prepend(follow);

                        visitedRuleName.add(ref.name());
                    }
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
            if (oneCycle.lastRef().isEmpty()) {
                removeSet.add(oneCycle);
                continue;
            }

            var start = oneCycle.startRule().get();
            var lastRef = oneCycle.lastRef().get();
            if (!start.name().equals(lastRef.name())) {
                removeSet.add(oneCycle);
            }
        }
        return removeSet;
    }
}
