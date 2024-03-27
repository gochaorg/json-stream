package xyz.cofe.grammar;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Tuple2;
import xyz.cofe.grammar.impl.Ascii;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    @SuppressWarnings("unused")
    public Optional<PathNode> lastRefNode() {
        return revPath().head();
    }

    /**
     * Рекурсия является "левой"
     *
     * @return true - левая рекурсия
     */
    public boolean isLeftRecursion() {
        return lastRefNode().map(n -> n.offset == 0).orElse(false);
    }

    @Override
    public String toString() {
        return "recursive ref" + (isLeftRecursion() ? "(left recursion)" : "") + ": rule="
            + startRule().map(Grammar.Rule::name).orElse("?")
            + " path=" + revPath().map(
                PathNode::toString
            ).reverse()
            .foldLeft("", (acc, it) -> acc.isBlank() ? it : acc + " > " + it)
            ;
    }

    /**
     * Узел пути
     *
     * @param rule    Правило в котором есть ссылка
     * @param defPath часть правила
     */
    public record PathNode(Grammar.Rule rule, Grammar.Definition.DefPath defPath, int offset) {
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

        public static PathNode of(Grammar.Rule rule) {
            if (rule == null) throw new IllegalArgumentException("rule==null");
            return new PathNode(rule, Grammar.Definition.DefPath.of(rule), 0);
        }

        public static PathNode of(Grammar.Rule rule, int offset) {
            if (rule == null) throw new IllegalArgumentException("rule==null");
            return new PathNode(rule, Grammar.Definition.DefPath.of(rule), offset);
        }
    }

    /**
     * Рекурсивный путь
     *
     * @param revPath путь
     */
    public record RecursivePath(
        ImList<PathNode> revPath
    ) {
        public static RecursivePath init(Grammar.Rule rule) {
            if (rule == null) throw new IllegalArgumentException("rule==null");
            return new RecursivePath(
                ImList.of(PathNode.of(rule))
            );
        }

        public RecursivePath add(PathNode node) {
            if (node == null) throw new IllegalArgumentException("node==null");
            return new RecursivePath(revPath.prepend(node));
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
            debugStartRule(start);

            var visitedRuleName = new HashSet<String>();
            visitedRuleName.add(start.name());

            ImList<RecursivePath> workSet = ImList.of(RecursivePath.init(start));

            while (workSet.size() > 0) {
                var headPath = workSet.head().get();

                debugShowHead(headPath);

                workSet = workSet.tail();

                var headNode = headPath.revPath().head().get();

                if (headNode.defPath.definition() instanceof Grammar.Ref ref) {
                    if (visitedRuleName.contains(ref.name())) {
                        // cycle detect
                        cycles.add(new RecursiveRef(headPath.revPath()));

                        debugAddCycle(headPath);
                    } else {
                        var follow =
                            grammar.rule(ref.name()).map(rule ->
                                headPath.add(PathNode.of(rule, headNode.offset))
                            );

                        debugShowFollow(follow, headNode);

                        workSet = workSet.prepend(follow);

                        visitedRuleName.add(ref.name());
                    }
                } else if (headNode.defPath.definition() instanceof Grammar.Alternative alt) {
                    var follow = alt.alt().map(d -> headPath.add(
                        new PathNode(headNode.rule, headNode.defPath.append(d), headNode.offset)
                    ));

                    debugShowFollow(follow, headNode);

                    workSet = workSet.prepend(follow);
                } else if (headNode.defPath.definition() instanceof Grammar.Sequence seq) {
                    var follow = seq.seq().foldLeft(
                        Tuple2.of(
                            ImList.<RecursivePath>of(),
                            headNode.offset
                        ),
                        (acc, it) -> acc.map((paths, offCounter) ->
                            {
                                int off =
                                    offCounter +
                                        switch (it) {
                                            case Grammar.Term ignore -> 1;
                                            default -> 0;
                                        };

                                return Tuple2.of(
                                    paths.append(
                                        headPath.add(
                                            new PathNode(
                                                headNode.rule,
                                                headNode.defPath.append(it),
                                                off
                                            )
                                        )
                                    ),
                                    off
                                );
                            }
                        )
                    ).map((paths, ignore) -> paths);

                    debugShowFollow(follow, headNode);

                    workSet = workSet.prepend(follow);
                } else if (headNode.defPath.definition() instanceof Grammar.Repeat rep) {
                    var follow1 =
                        headPath.add(
                            new PathNode(
                                headNode.rule,
                                headNode.defPath.append(rep.def()),
                                headNode.offset
                            )
                        );

                    var follow = ImList.of(List.of(follow1));

                    debugShowFollow(follow, headNode);

                    workSet = workSet.prepend(follow);
                }
            }
        });

        var removeSet = getInvalidPaths(cycles);
        cycles.removeAll(removeSet);

        var result = ImList.of(cycles);
        cache.put(grammar, result);

        return result;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean debugEnable() {return false;}

    private static void debugAddCycle(RecursivePath headPath) {
        if (!debugEnable()) return;

        System.out.println(Ascii.bold + "recursive " + Ascii.reset +

            Ascii.Color.Magenta.foreground() + Ascii.bold +
            headPath.revPath().reverse().map(PathNode::toString)
                .foldLeft("", (acc, it) -> !acc.isEmpty() ? acc + " > " + it : it)

            + Ascii.reset
        );
    }

    private static void debugStartRule(Grammar.Rule start) {
        if (!debugEnable()) return;

        System.out.println("start " + Ascii.Color.Red.foreground() + Ascii.bold + start.name() + Ascii.reset);
    }

    private static void debugShowHead(RecursivePath r_path) {
        if (!debugEnable()) return;

        System.out.println(Ascii.Color.White.foreground() + "head path " + Ascii.reset +
            r_path.revPath().reverse()
                .map(PathNode::toString)
                .foldLeft("", (acc, it) -> !acc.isEmpty() ? acc + " > " + it : it)
        );
    }

    private static void debugShowFollow(ImList<RecursivePath> follow, PathNode headNode) {
        if (!debugEnable()) return;

        System.out.println(Ascii.Color.White.foreground() + "follow (" + follow.size() + ") from " + Ascii.reset + headNode);

        follow.each(path -> System.out.println("  " + path.revPath.reverse()
            .map(PathNode::toString)
            .foldLeft("", (acc, it) -> !acc.isEmpty() ? acc + " > " + it : it)
        ));
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
