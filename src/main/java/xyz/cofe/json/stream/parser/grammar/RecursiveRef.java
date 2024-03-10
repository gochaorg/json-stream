package xyz.cofe.json.stream.parser.grammar;

import xyz.cofe.coll.im.ImList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    @SuppressWarnings("unused")
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
     * @param rule    Правило в котором есть ссылка
     * @param defPath часть правила
     */
    public record PathNode(Grammar.Rule rule, Grammar.Definition.DefPath defPath, int offset) {
        @Override
        public String toString() {
            var defTxt = switch (defPath.definition()) {
                case Grammar.Ref(var r) -> "ref(" + r + " o=" + offset + ")";
                case Grammar.Term(var t) -> "term(" + t + " o=" + offset + ")";
                case Grammar.Alternative ignored -> "alt(o=" + offset + ")";
                case Grammar.Repeat ignored -> "repeat(o=" + offset + ")";
                case Grammar.Sequence ignored -> "sequence(o=" + offset + ")";
            };
            return rule.name() + "/" + defTxt;
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
     * @param revPath путь
     */
    public record RecursivePath(
        ImList<PathNode> revPath
    ) {
        public static RecursivePath init(Grammar.Rule rule){
            if( rule==null ) throw new IllegalArgumentException("rule==null");
            return new RecursivePath(
                ImList.of(PathNode.of(rule))
            );
        }

        public RecursivePath add(PathNode node){
            if( node==null ) throw new IllegalArgumentException("node==null");
            return new RecursivePath( revPath.prepend(node) );
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
            System.out.println("start " + start.name());

            var visitedRuleName = new HashSet<String>();
            visitedRuleName.add(start.name());

            ImList<RecursivePath> workSet = ImList.of(RecursivePath.init(start));

            while (workSet.size() > 0) {
                var headPath = workSet.head().get();

                // TODO debug
                dubugShowHead(headPath);

                workSet = workSet.tail();

                var headNode = headPath.revPath().head().get();

                if (headNode.defPath.definition() instanceof Grammar.Ref ref) {
                    if (visitedRuleName.contains(ref.name())) {
                        // cycle detect
                        cycles.add(new RecursiveRef(headPath.revPath()));
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
                    var follow = seq.seq().enumerate().map(
                        d -> headPath.add(
                            new PathNode(headNode.rule, headNode.defPath.append(d.value()), headNode.offset)
                        )
                    );

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

    private static void dubugShowHead(RecursivePath r_path) {
        System.out.println("head path " +
            r_path.revPath().reverse()
                .map(PathNode::toString)
                .foldLeft("", (acc, it) -> !acc.isEmpty() ? acc + " > " + it : it)
        );
    }

    private static void debugShowFollow(ImList<RecursivePath> follow, PathNode headNode) {
        // TODO debug
        System.out.println("follow (" + follow.size() + ") from " + headNode);

        // TODO debug
        follow.each(path -> System.out.println(path.revPath.reverse()
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
