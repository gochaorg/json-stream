package xyz.cofe.grammar;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Tuple2;
import xyz.cofe.grammar.impl.Ascii;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class RecursiveVisitor {
    public final Grammar grammar;
    private final HashSet<String> visitedRuleName;
    private ImList<RecursivePath> workSet;
    private ImList<RecursiveRef> cycles;
    private final Weight weight;

    public static RecursiveVisitor visit(Grammar grammar, Grammar.Rule start, Consumer<RecursivePath> currentPath){
        if( grammar==null ) throw new IllegalArgumentException("grammar==null");
        if( start==null ) throw new IllegalArgumentException("start==null");

        RecursiveVisitor vistor = new RecursiveVisitor(grammar);
        vistor.start(start);
        while(vistor.next(currentPath)){
        }

        return vistor;
    }

    public static RecursiveVisitor visit(Grammar grammar, Grammar.Rule start){
        if( grammar==null ) throw new IllegalArgumentException("grammar==null");
        if( start==null ) throw new IllegalArgumentException("start==null");
        return visit(grammar, start, ignore -> {});
    }

    public RecursiveVisitor(Grammar grammar){
        if( grammar==null ) throw new IllegalArgumentException("grammar==null");
        this.grammar = grammar;
        this.visitedRuleName = new HashSet<>();
        this.workSet = ImList.of();
        this.cycles = ImList.of();
        this.weight = new Weight(grammar);
    }

    public Set<String> visitedRuleNames(){ return visitedRuleName; }

    public void start(Grammar.Rule rule){
        if( rule==null ) throw new IllegalArgumentException("rule==null");

        debugStartRule(rule);

        workSet = ImList.of(RecursivePath.init(rule));
        visitedRuleName.clear();
        cycles = ImList.of();
    }

    public boolean next(Consumer<RecursivePath> currentPath){
        if( currentPath==null ) throw new IllegalArgumentException("currentPath==null");
        if( workSet.size() < 1 )return false;

        workSet = flow(workSet, currentPath);
        return true;
    }

    public ImList<RecursiveRef> cycles(){ return cycles; }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private ImList<RecursivePath> flow(ImList<RecursivePath> workSet, Consumer<RecursivePath> currentPath) {
        RecursivePath headPath = workSet.head().get();
        currentPath.accept(headPath);

        debugShowHead(headPath);

        workSet = workSet.tail();

        var headNode = headPath.revPath().head().get();

        if (headNode.defPath().definition() instanceof Grammar.Ref ref) {
            if (visitedRuleName.contains(ref.name())) {
                // cycle detect
                cycles = cycles.append(new RecursiveRef(headPath.revPath()));

                debugAddCycle(headPath);
            } else {
                var follow =
                    grammar.rule(ref.name()).map(rule ->
                        headPath.add(RecursiveNode.of(rule, headNode.offset()))
                    );

                debugShowFollow(follow, headNode);

                workSet = workSet.prepend(follow);

                visitedRuleName.add(ref.name());
            }
        } else if (headNode.defPath().definition() instanceof Grammar.Alternative alt) {
            var follow = alt.alt().map(d -> headPath.add(
                new RecursiveNode(headNode.rule(), headNode.defPath().append(d), headNode.offset())
            ));

            debugShowFollow(follow, headNode);

            workSet = workSet.prepend(follow);
        } else if (headNode.defPath().definition() instanceof Grammar.Sequence seq) {
            var follow = seq.seq().foldLeft(
                Tuple2.of(
                    ImList.<RecursivePath>of(),
                    headNode.offset()
                ),
                (acc, it) -> acc.map((paths, offCounter) ->
                    {
                        int weight = 0;
                        if( it instanceof Grammar.Term ){
                            weight = 1;
                        }else {
                            weight = this.weight.weightOf(it);
                        }

                        int nextOffset = offCounter + weight;

                        return Tuple2.of(
                            paths.append(
                                headPath.add(
                                    new RecursiveNode(
                                        headNode.rule(),
                                        headNode.defPath().append(it),
                                        offCounter
                                    )
                                )
                            ),
                            nextOffset
                        );
                    }
                )
            ).map((paths, ignore) -> paths);

            debugShowFollow(follow, headNode);

            workSet = workSet.prepend(follow);
        } else if (headNode.defPath().definition() instanceof Grammar.Repeat rep) {
            var follow1 =
                headPath.add(
                    new RecursiveNode(
                        headNode.rule(),
                        headNode.defPath().append(rep.def()),
                        headNode.offset()
                    )
                );

            var follow = ImList.of(List.of(follow1));

            debugShowFollow(follow, headNode);

            workSet = workSet.prepend(follow);
        }
        return workSet;
    }

    private static Boolean debugEnable;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean debugEnable() {
        if( debugEnable!=null )return debugEnable;
        debugEnable = "true".equalsIgnoreCase(System.getProperty(RecursiveVisitor.class.getName()+".debug", "false"));
        return debugEnable;
    }

    private static void debugAddCycle(RecursivePath headPath) {
        if (!debugEnable()) return;

        System.out.println(Ascii.bold + "recursive " + Ascii.reset +

            Ascii.Color.Magenta.foreground() + Ascii.bold +
            headPath.revPath().reverse().map(RecursiveNode::toString)
                .foldLeft("", (acc, it) -> !acc.isEmpty() ? acc + " > " + it : it)

            + Ascii.reset
        );
    }

    static void debugStartRule(Grammar.Rule start) {
        if (!debugEnable()) return;

        System.out.println("start " + Ascii.Color.Red.foreground() + Ascii.bold + start.name() + Ascii.reset);
    }

    private static void debugShowHead(RecursivePath r_path) {
        if (!debugEnable()) return;

        System.out.println(Ascii.Color.White.foreground() + "head path " + Ascii.reset +
            r_path.revPath().reverse()
                .map(RecursiveNode::toString)
                .foldLeft("", (acc, it) -> !acc.isEmpty() ? acc + " > " + it : it)
        );
    }

    private static void debugShowFollow(ImList<RecursivePath> follow, RecursiveNode headNode) {
        if (!debugEnable()) return;

        System.out.println(Ascii.Color.White.foreground() + "follow (" + follow.size() + ") from " + Ascii.reset + headNode);

        follow.each(path -> System.out.println("  " + path.revPath().reverse()
            .map(RecursiveNode::toString)
            .foldLeft("", (acc, it) -> !acc.isEmpty() ? acc + " > " + it : it)
        ));
    }
}
